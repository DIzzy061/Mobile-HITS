package com.example.codeblockhits.data

sealed class VariableValue {
    data class Scalar(val value: String) : VariableValue()
    data class Array(val values: MutableList<String>) : VariableValue()
}

data class InterpreterResult(
    val output: List<String>,
    val variables: Map<String, VariableValue>,
    val errorBlockId: Int? = null
)

data class RpnResult(val value: String, val isError: Boolean = false, val errorMessage: String = "")

fun evaluateExpression(expression: String, variables: Map<String, VariableValue>): String {
    var processedExpr = expression
    variables.forEach { (name, value) ->
        val replacement = when (value) {
            is VariableValue.Scalar -> value.value
            is VariableValue.Array -> value.values.joinToString(",")
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
    return object : Any() {
        var pos = -1
        var ch = 0.toChar()
        fun nextChar() { ch = if (++pos < expression.length) expression[pos] else (-1).toChar() }
        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }
        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expression.length) throw RuntimeException("Unexpected: $ch")
            return x
        }
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }
        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> x /= parseFactor()
                    eat('%') -> x %= parseFactor()
                    else -> return x
                }
            }
        }
        fun parseFactor(): Double {
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()
            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if (ch in '0'..'9' || ch == '.') {
                while (ch in '0'..'9' || ch == '.') nextChar()
                x = expression.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: $ch")
            }
            if (eat('^')) x = Math.pow(x, parseFactor())
            return x
        }
    }.parse()
}

fun evalRpn(expr: String, variables: Map<String, VariableValue>): RpnResult {
    val tokens = expr.trim().split(" ").filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return RpnResult("", isError = true, errorMessage = "Expression is empty")

    val arrayAccessRegex = Regex("""([a-zA-Z_][a-zA-Z0-9_]*)\[(.+)]""")
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
                if (variable == null) {
                    return RpnResult("", true, "Variable '$token' not initialized.")
                }

                when (variable) {
                    is VariableValue.Scalar -> {
                        val value = variable.value.toDoubleOrNull()
                            ?: return RpnResult("", true, "Invalid number in scalar '$token'")
                        stack.addLast(value)
                    }
                    is VariableValue.Array -> {
                        return RpnResult("", true, "Use indexed access for array '$token', e.g. $token[0]")
                    }
                }
            }

            arrayAccessRegex.matches(token) -> {
                val (name, indexExpr) = arrayAccessRegex.matchEntire(token)!!.destructured
                val array = variables[name]
                if (array !is VariableValue.Array) return RpnResult("", true, "'$name' is not an array")

                val indexResult = evalRpn(indexExpr, variables)
                if (indexResult.isError) return indexResult
                val index = indexResult.value.toDoubleOrNull()?.toInt()
                    ?: return RpnResult("", true, "Invalid index '$indexExpr'")
                if (index !in array.values.indices) return RpnResult("", true, "Index $index out of bounds for '$name'")
                val value = array.values[index].toDoubleOrNull()
                    ?: return RpnResult("", true, "Array element '$name[$index]' is not a number")
                stack.addLast(value)
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
        val block = blockMap[currentId] ?: return InterpreterResult(output.apply {
            add("Error: Block with ID $currentId not found.")
        }, variables, currentId)

        try {
            when (block) {
                is VariableBlock -> {
                    if (!variables.containsKey(block.name)) {
                        val evaluatedValue = evaluateExpression(block.value, variables)
                        if (evaluatedValue.startsWith("Error:")) {
                            return InterpreterResult(output + "Error in Variable '${block.name}': $evaluatedValue", variables, block.id)
                        }
                        variables[block.name] = VariableValue.Scalar(evaluatedValue)
                    }
                }
                is AssignmentBlock -> {
                    val evaluatedValue = evaluateExpression(block.expression, variables)
                    if (evaluatedValue.startsWith("Error:")) {
                        return InterpreterResult(output + "Error in Assignment to '${block.target}': $evaluatedValue", variables, block.id)
                    }
                    variables[block.target] = VariableValue.Scalar(evaluatedValue)
                }
                is WhileBlock -> {
                    var safeguard = 1000
                    val match = Regex("(.+?)\\s*(==|!=|>|<|>=|<=)\\s*(.+)").find(block.condition)
                    if (match != null) {
                        val (leftExpr, op, rightExpr) = match.destructured
                        while (safeguard-- > 0) {
                            val left = evalRpn(leftExpr, variables)
                            val right = evalRpn(rightExpr, variables)
                            if (left.isError || right.isError) {
                                return InterpreterResult(output + "Error in While condition: ${left.errorMessage} ${right.errorMessage}".trim(), variables, block.id)
                            }
                            val l = left.value.toDoubleOrNull()
                            val r = right.value.toDoubleOrNull()
                            if (l == null || r == null) {
                                return InterpreterResult(output + "Error in While condition: Operands must be numbers.", variables, block.id)
                            }
                            val cond = when (op) {
                                "==" -> l == r
                                "!=" -> l != r
                                ">"  -> l > r
                                "<"  -> l < r
                                ">=" -> l >= r
                                "<=" -> l <= r
                                else -> false
                            }
                            if (!cond) break
                            val loopResult = interpretBlocksRPN(
                                block.innerBlocks,
                                startBlockId = block.innerBlocks.firstOrNull()?.id,
                                variables = variables.toMutableMap()
                            )
                            output.addAll(loopResult.output)
                            if (loopResult.errorBlockId != null) return loopResult
                            variables.putAll(loopResult.variables)
                        }
                    } else {
                        while (safeguard-- > 0) {
                            val conditionResult = evalRpn(block.condition, variables)
                            if (conditionResult.isError) {
                                return InterpreterResult(output + "Error in While condition: ${conditionResult.errorMessage}", variables, block.id)
                            }
                            val condition = conditionResult.value.toDoubleOrNull() ?: return InterpreterResult(
                                output + "While condition is not a number: '${conditionResult.value}'", variables, block.id
                            )
                            if (condition == 0.0) break
                            val loopResult = interpretBlocksRPN(
                                block.innerBlocks,
                                startBlockId = block.innerBlocks.firstOrNull()?.id,
                                variables = variables.toMutableMap()
                            )
                            output.addAll(loopResult.output)
                            if (loopResult.errorBlockId != null) return loopResult
                            variables.putAll(loopResult.variables)
                        }
                    }
                }

                is PrintBlock -> {
                    val result = block.expressions.map {
                        val res = evalRpn(it, variables)
                        if (res.isError) return InterpreterResult(output + "Error in Print '$it': ${res.errorMessage}", variables, block.id)
                        res.value
                    }
                    output.add(result.joinToString(", "))
                }
                is IfElseBlock -> {
                    val left = evalRpn(block.leftOperand, variables)
                    val right = evalRpn(block.rightOperand, variables)
                    if (left.isError || right.isError) {
                        return InterpreterResult(output + "Error in If condition: ${left.errorMessage} ${right.errorMessage}".trim(), variables, block.id)
                    }

                    val l = left.value.toDoubleOrNull()
                    val r = right.value.toDoubleOrNull()
                    if (l == null || r == null) {
                        return InterpreterResult(output + "Error in If condition: Operands must be numbers.", variables, block.id)
                    }

                    val result = when (block.operator) {
                        "==" -> l == r
                        "!=" -> l != r
                        ">"  -> l > r
                        "<"  -> l < r
                        ">=" -> l >= r
                        "<=" -> l <= r
                        else -> false
                    }

                    val chosenBranch = if (result) block.thenBlocks else block.elseBlocks
                    val branchResult = interpretBlocksRPN(chosenBranch, startBlockId = chosenBranch.firstOrNull()?.id, variables = variables.toMutableMap())
                    output.addAll(branchResult.output)
                    if (branchResult.errorBlockId != null) return branchResult
                    variables.putAll(branchResult.variables)
                }
            }
        } catch (e: Exception) {
            return InterpreterResult(output + "Runtime error at block ${block.id}: ${e.message}", variables, block.id)
        }

        currentId = when (block) {
            is VariableBlock -> block.nextBlockId
            is AssignmentBlock -> block.nextBlockId
            is PrintBlock -> block.nextBlockId
            is IfElseBlock -> block.nextBlockId
            is WhileBlock -> block.nextBlockId
        }
    }

    return InterpreterResult(output, variables, null)
}
