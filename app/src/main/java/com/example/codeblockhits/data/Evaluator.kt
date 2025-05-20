package com.example.codeblockhits.data


fun evaluateExpression(expression: String, variables: Map<String, String>): String {
    return try {

        var processedExpr = expression
        variables.forEach { (name, value) ->
            processedExpr = processedExpr.replace(name, value)
        }


        val result =evaluateMathExpression(processedExpr)
        result.toString()
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun evaluateBlock(block: CodeBlock, variables: Map<String, String>): CodeBlock {
    return when (block) {
        is VariableBlock -> block.copy(value = evaluateExpression(block.value, variables))
        is AssignmentBlock -> {
            val evaluated = evaluateExpression(block.expression, variables)
            AssignmentBlock(id = block.id, target = block.target, expression = evaluated)
        }
        is IfElseBlock -> {
            val left = evaluateExpression(block.leftOperand, variables).toDoubleOrNull()
            val right = evaluateExpression(block.rightOperand, variables).toDoubleOrNull()
            val condition = when (block.operator) {
                "==" -> left == right
                "!=" -> left != right
                ">" -> left != null && right != null && left > right
                "<" -> left != null && right != null && left < right
                ">=" -> left != null && right != null && left >= right
                "<=" -> left != null && right != null && left <= right
                else -> false
            }

            val newThen = if (condition) {
                block.thenBlocks.map {
                    if (it is VariableBlock)
                        it.copy(value = evaluateExpression(it.value, variables))
                    else it
                }
            } else block.thenBlocks

            val newElse = if (!condition) {
                block.elseBlocks.map {
                    if (it is VariableBlock)
                        it.copy(value = evaluateExpression(it.value, variables))
                    else it
                }
            } else block.elseBlocks

            block.copy(thenBlocks = newThen, elseBlocks = newElse)
        }
        else -> block
    }
}

fun evaluateMathExpression(expression: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0.toChar()

        fun nextChar() {
            ch = if (++pos < expression.length) expression[pos] else (-1).toChar()
        }

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
            if (pos < expression.length) throw RuntimeException("Невозможно " + ch)
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
                throw RuntimeException("Невозможно " + ch)
            }

            if (eat('^')) x = Math.pow(x, parseFactor())

            return x
        }
    }.parse()
}

data class InterpreterResult(
    val output: List<String>,
    val variables: Map<String, String>,
    val errorBlockId: Int? = null
)

fun interpretBlocksRPN(
    blocks: List<CodeBlock>,
    startBlockId: Int? = null,
    variables: MutableMap<String, String> = mutableMapOf()
): InterpreterResult {
    val blockMap = blocks.associateBy { it.id }
    val output = mutableListOf<String>()
    val visited = mutableSetOf<Int>()
    var currentId = startBlockId ?: blocks.firstOrNull()?.id

    while (currentId != null && currentId !in visited) {
        visited.add(currentId)
        val block = blockMap[currentId] ?: run {
            output.add("Error: Block with ID $currentId not found.")
            return InterpreterResult(output, variables, currentId)
        }

        try {
            when (block) {
                is VariableBlock -> {
                    val valueResult = evalRpn(block.value, variables)
                    if (valueResult.isError) {
                        output.add("Error in Variable '${block.name}': ${valueResult.errorMessage}")
                        return InterpreterResult(output, variables, block.id)
                    }
                    variables[block.name] = valueResult.value
                }
                is AssignmentBlock -> {
                    val valueResult = evalRpn(block.expression, variables)
                    if (valueResult.isError) {
                        output.add("Error in Assignment to '${block.target}': ${valueResult.errorMessage}")
                        return InterpreterResult(output, variables, block.id)
                    }
                    variables[block.target] = valueResult.value
                }
                is PrintBlock -> {
                    val results = mutableListOf<String>()
                    for (expr in block.expressions) {
                        val valueResult = evalRpn(expr, variables)
                        if (valueResult.isError) {
                            output.add("Error in Print expression '$expr': ${valueResult.errorMessage}")
                            return InterpreterResult(output, variables, block.id)
                        }
                        results.add(valueResult.value)
                    }
                    output.add(results.joinToString(", "))
                }
                is IfElseBlock -> {
                    val leftResult = evalRpn(block.leftOperand, variables)
                    if (leftResult.isError) {
                        output.add("Error in If condition (left operand '${block.leftOperand}'): ${leftResult.errorMessage}")
                        return InterpreterResult(output, variables, block.id)
                    }
                    val rightResult = evalRpn(block.rightOperand, variables)
                    if (rightResult.isError) {
                        output.add("Error in If condition (right operand '${block.rightOperand}'): ${rightResult.errorMessage}")
                        return InterpreterResult(output, variables, block.id)
                    }

                    val left = leftResult.value.toDoubleOrNull()
                    val right = rightResult.value.toDoubleOrNull()

                    if (left == null || right == null) {
                         output.add("Error in If condition: Operands must be numbers. Got '${leftResult.value}' and '${rightResult.value}'")
                         return InterpreterResult(output, variables, block.id)
                    }

                    val condition = when (block.operator) {
                        "==" -> left == right
                        "!=" -> left != right
                        ">" -> left > right
                        "<" -> left < right
                        ">=" -> left >= right
                        "<=" -> left <= right
                        else -> {
                            output.add("Error: Unknown operator '${block.operator}' in If condition.")
                            return InterpreterResult(output, variables, block.id)
                        }
                    }
                    val branchToExecute = if (condition) block.thenBlocks else block.elseBlocks

                    val linkedBranch = branchToExecute.mapIndexed { idx, b ->
                        if (idx < branchToExecute.size - 1 && (b.nextBlockId == null || b.nextBlockId == 0)) {
                            when (b) {
                                is VariableBlock -> b.copy(nextBlockId = branchToExecute[idx + 1].id)
                                is AssignmentBlock -> b.copy(nextBlockId = branchToExecute[idx + 1].id)
                                is PrintBlock -> b.copy(nextBlockId = branchToExecute[idx + 1].id)
                                is IfElseBlock -> b.copy(nextBlockId = branchToExecute[idx + 1].id)
                                else -> b
                            }
                        } else b
                    }
                    
                    val branchVariables = variables.toMutableMap()
                    val branchResult = interpretBlocksRPN(linkedBranch, startBlockId = linkedBranch.firstOrNull()?.id, variables = branchVariables)
                    
                    output.addAll(branchResult.output)
                    
                    if (branchResult.errorBlockId != null) {
                        return InterpreterResult(output, branchResult.variables, branchResult.errorBlockId)
                    }
                    variables.putAll(branchResult.variables)
                }
            }
        } catch (e: Exception) {
            output.add("Runtime Error in block ID ${block.id} ('${block::class.simpleName}'): ${e.message}")
            return InterpreterResult(output, variables, block.id)
        }

        currentId = when (block) {
            is VariableBlock -> block.nextBlockId
            is AssignmentBlock -> block.nextBlockId
            is IfElseBlock -> block.nextBlockId
            is PrintBlock -> block.nextBlockId
        }
    }
    return InterpreterResult(output, variables, null)
}

data class RpnResult(val value: String, val isError: Boolean = false, val errorMessage: String = "")

fun evalRpn(expr: String, variables: Map<String, String>): RpnResult {
    val tokens = expr.trim().split(" ").filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return RpnResult("", isError = true, errorMessage = "Expression is empty")

    val stack = ArrayDeque<Double>()
    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
            variables.containsKey(token) -> {
                val variableValueString = variables[token]
                val variableValue = variableValueString?.toDoubleOrNull()
                if (variableValue == null) {
                    return RpnResult("", true, "Variable '$token' (value: '$variableValueString') is not a valid number or not initialized properly.")
                }
                stack.addLast(variableValue)
            }
            token in setOf("+", "-", "*", "/", "%", "^") -> {
                if (stack.size < 2) return RpnResult("", true, "Not enough operands for operator '$token'. Current stack: $stack")
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b == 0.0) return RpnResult("", true, "Division by zero ('$a / $b')") else a / b
                    "%" -> if (b == 0.0) return RpnResult("", true, "Modulo by zero ('$a % $b')") else a % b
                    "^" -> Math.pow(a, b)
                    else -> return RpnResult("", true, "Unknown operator '$token'")
                }
                stack.addLast(res)
            }
            else -> return RpnResult("", true, "Unknown token or uninitialized variable '$token' in expression.")
        }
    }
    return if (stack.size == 1) RpnResult(stack.last().toString(), false) else RpnResult("", true, "Malformed expression. Check operators and operands. Final stack: $stack")
}





