package com.example.codeblockhits.data

sealed class VariableValue {
    data class Scalar(val value: String) : VariableValue()
}

data class InterpreterResult(
    val output: List<String>,
    val variables: Map<String, VariableValue>,
    val errorBlockId: Int? = null
)

data class RpnResult(val value: String, val isError: Boolean = false, val errorMessage: String = "")

private fun linkBlocksSequentially(blocks: List<CodeBlock>): List<CodeBlock> {
    return blocks.mapIndexed { index, block ->
        val nextId = blocks.getOrNull(index + 1)?.id
        when (block) {
            is VariableBlock -> block.copy(nextBlockId = nextId)
            is AssignmentBlock -> block.copy(nextBlockId = nextId)
            is PrintBlock -> block.copy(nextBlockId = nextId)
            is IfElseBlock -> block.copy(nextBlockId = nextId)
            is WhileBlock -> block.copy(nextBlockId = nextId)
        }
    }
}

fun evaluateExpression(expression: String, variables: Map<String, VariableValue>): String {
    var processedExpr = expression
    variables.forEach { (name, value) ->
        val replacement = when (value) {
            is VariableValue.Scalar -> value.value
        }
        processedExpr = processedExpr.replace(name, replacement)
    }

    return try {
        evaluateMathExpression(processedExpr).toString()
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun evaluateMathExpression(expression: String): Double {
    val tokens = expression.trim().split(Regex("(?<=[+\\-*/%^()])|(?=[+\\-*/%^()])")).filter { it.isNotEmpty() }
    val stack = mutableListOf<Double>()
    val operators = mutableListOf<String>()
    
    fun precedence(op: String): Int = when(op) {
        "+", "-" -> 1
        "*", "/", "%" -> 2
        "^" -> 3
        else -> 0
    }
    
    fun applyOperator(op: String) {
        if (stack.size < 2) throw RuntimeException("Not enough operands for operator '$op'")
        val b = stack.removeAt(stack.lastIndex)
        val a = stack.removeAt(stack.lastIndex)
        val result = when(op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b == 0.0) throw RuntimeException("Division by zero") else a / b
            "%" -> if (b == 0.0) throw RuntimeException("Modulo by zero") else a % b
            "^" -> Math.pow(a, b)
            else -> throw RuntimeException("Unknown operator '$op'")
        }
        stack.add(result)
    }
    
    for (token in tokens) {
        when {
            token.matches(Regex("-?\\d+(\\.\\d+)?")) -> stack.add(token.toDouble())
            token == "(" -> operators.add(token)
            token == ")" -> {
                while (operators.isNotEmpty() && operators.last() != "(") {
                    applyOperator(operators.removeAt(operators.lastIndex))
                }
                if (operators.isEmpty() || operators.last() != "(") {
                    throw RuntimeException("Mismatched parentheses")
                }
                operators.removeAt(operators.lastIndex)
            }
            token in setOf("+", "-", "*", "/", "%", "^") -> {
                while (operators.isNotEmpty() && 
                       operators.last() != "(" && 
                       precedence(operators.last()) >= precedence(token)) {
                    applyOperator(operators.removeAt(operators.lastIndex))
                }
                operators.add(token)
            }
            else -> throw RuntimeException("Invalid token: '$token'")
        }
    }
    
    while (operators.isNotEmpty()) {
        if (operators.last() == "(") throw RuntimeException("Mismatched parentheses")
        applyOperator(operators.removeAt(operators.lastIndex))
    }
    
    if (stack.size != 1) throw RuntimeException("Invalid expression")
    return stack[0]
}

fun evalRpn(expr: String, variables: Map<String, VariableValue>): RpnResult {
    val tokens = expr.trim().split(" ").filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return RpnResult("", isError = true, errorMessage = "Expression is empty")

    val stack = ArrayDeque<Double>()

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())

            token in setOf("+", "-", "*", "/", "%", "^") -> {
                if (stack.size < 2) return RpnResult("", true, "Not enough operands for operator '$token'")
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b == 0.0) return RpnResult("", true, "Division by zero") else a / b
                    "%" -> if (b == 0.0) return RpnResult("", true, "Modulo by zero") else a % b
                    "^" -> Math.pow(a, b)
                    else -> return RpnResult("", true, "Unknown operator '$token'")
                }
                stack.addLast(res)
            }

            variables.containsKey(token) -> {
                val variable = variables[token]
                when (variable) {
                    is VariableValue.Scalar -> {
                        val value = variable.value.toDoubleOrNull()
                            ?: return RpnResult("", true, "Invalid number in scalar '$token'")
                        stack.addLast(value)
                    }
                    null -> return RpnResult("", true, "Variable '$token' not initialized.")
                }
            }

            else -> return RpnResult("", true, "Unknown token '$token'")
        }
    }

    return if (stack.size == 1) RpnResult(stack.last().toString())
    else RpnResult("", true, "Malformed RPN expression. Stack: $stack")
}

fun interpretBlocksRPN(
    blocks: List<CodeBlock>,
    startBlockId: Int? = null,
    variables: MutableMap<String, VariableValue> = mutableMapOf()
): InterpreterResult {
    val blockMap = blocks.associateBy { it.id }
    val output = mutableListOf<String>()
    val visited = mutableSetOf<Int>()
    var currentId = startBlockId ?: blocks.firstOrNull()?.id

    while (currentId != null && currentId !in visited) {
        visited.add(currentId)
        val block = blockMap[currentId] ?: return InterpreterResult(output + "Error: Block with ID $currentId not found.", variables, currentId)

        try {
            when (block) {
                is VariableBlock -> {
                    if (!variables.containsKey(block.name)) {
                        val value = evaluateExpression(block.value, variables)
                        if (value.startsWith("Error:")) return InterpreterResult(output + "Error in Variable '${block.name}': $value", variables, block.id)
                        variables[block.name] = VariableValue.Scalar(value)
                    }
                }
                is AssignmentBlock -> {
                    val value = evaluateExpression(block.expression, variables)
                    if (value.startsWith("Error:")) return InterpreterResult(output + "Error in Assignment '${block.target}': $value", variables, block.id)
                    variables[block.target] = VariableValue.Scalar(value)
                }
                is PrintBlock -> {
                    val result = block.expressions.map {
                        try {
                            val value = evaluateExpression(it, variables)
                            if (value.startsWith("Error:")) {
                                return InterpreterResult(output + "Error in Print '$it': $value", variables, block.id)
                            }
                            value
                        } catch (e: Exception) {
                            return InterpreterResult(output + "Error in Print '$it': ${e.message}", variables, block.id)
                        }
                    }
                    output.add(result.joinToString(", "))
                }
                is IfElseBlock -> {
                    val left = evalRpn(block.leftOperand, variables)
                    val right = evalRpn(block.rightOperand, variables)
                    if (left.isError || right.isError) {
                        return InterpreterResult(output + "Error in If: ${left.errorMessage} ${right.errorMessage}".trim(), variables, block.id)
                    }
                    val l = left.value.toDoubleOrNull()
                    val r = right.value.toDoubleOrNull()
                    if (l == null || r == null) return InterpreterResult(output + "Error in If: operands must be numeric", variables, block.id)
                    val condition = when (block.operator) {
                        "==" -> l == r; "!=" -> l != r
                        ">" -> l > r; "<" -> l < r
                        ">=" -> l >= r; "<=" -> l <= r
                        else -> false
                    }
                    val inner = linkBlocksSequentially(if (condition) block.thenBlocks else block.elseBlocks)
                    val result = interpretBlocksRPN(inner, inner.firstOrNull()?.id, variables.toMutableMap())
                    output.addAll(result.output)
                    if (result.errorBlockId != null) return result
                    variables.putAll(result.variables)
                }
                is WhileBlock -> {
                    val inner = linkBlocksSequentially(block.innerBlocks)
                    var safeguard = 1000
                    while (safeguard-- > 0) {
                        val left = evalRpn(block.leftOperand, variables)
                        val right = evalRpn(block.rightOperand, variables)
                        if (left.isError || right.isError) {
                            return InterpreterResult(output + "Error in While: ${left.errorMessage} ${right.errorMessage}".trim(), variables, block.id)
                        }
                        val l = left.value.toDoubleOrNull()
                        val r = right.value.toDoubleOrNull()
                        if (l == null || r == null) return InterpreterResult(output + "Error in While: operands must be numeric", variables, block.id)
                        val condition = when (block.operator) {
                            "==" -> l == r; "!=" -> l != r
                            ">" -> l > r; "<" -> l < r
                            ">=" -> l >= r; "<=" -> l <= r
                            else -> false
                        }
                        if (!condition) break
                        val result = interpretBlocksRPN(inner, inner.firstOrNull()?.id, variables.toMutableMap())
                        output.addAll(result.output)
                        if (result.errorBlockId != null) return result
                        variables.putAll(result.variables)
                    }
                }
            }
        } catch (e: Exception) {
            return InterpreterResult(output + "Runtime error at block ${block.id}: ${e.message}", variables, block.id)
        }

        currentId = block.nextBlockId
    }

    return InterpreterResult(output, variables)
}

