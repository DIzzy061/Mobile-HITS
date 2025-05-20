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
    val variables: Map<String, String>
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
        val block = blockMap[currentId] ?: break
        when (block) {
            is VariableBlock -> {
                val value = evalRpn(block.value, variables)
                variables[block.name] = value
            }
            is AssignmentBlock -> {
                val value = evalRpn(block.expression, variables)
                variables[block.target] = value
            }
            is PrintBlock -> {
                val results = block.expressions.map { evalRpn(it, variables) }
                output.add(results.joinToString(", "))
            }
            is IfElseBlock -> {
                val left = evalRpn(block.leftOperand, variables).toDoubleOrNull()
                val right = evalRpn(block.rightOperand, variables).toDoubleOrNull()
                val condition = when (block.operator) {
                    "==" -> left == right
                    "!=" -> left != right
                    ">" -> left != null && right != null && left > right
                    "<" -> left != null && right != null && left < right
                    ">=" -> left != null && right != null && left >= right
                    "<=" -> left != null && right != null && left <= right
                    else -> false
                }
                val branch = if (condition) block.thenBlocks else block.elseBlocks

                val linkedBranch = branch.mapIndexed { idx, b ->
                    if (idx < branch.size - 1 && (b.nextBlockId == null || b.nextBlockId == 0)) {
                        when (b) {
                            is VariableBlock -> b.copy(nextBlockId = branch[idx + 1].id)
                            is AssignmentBlock -> b.copy(nextBlockId = branch[idx + 1].id)
                            is PrintBlock -> b.copy(nextBlockId = branch[idx + 1].id)
                            is IfElseBlock -> b.copy(nextBlockId = branch[idx + 1].id)
                            else -> b
                        }
                    } else b
                }

                val branchResult = interpretBlocksRPN(linkedBranch, variables = variables.toMutableMap())
                output.addAll(branchResult.output)
                variables.putAll(branchResult.variables)
            }
        }
        currentId = when (block) {
            is VariableBlock -> block.nextBlockId
            is AssignmentBlock -> block.nextBlockId
            is IfElseBlock -> block.nextBlockId
            is PrintBlock -> block.nextBlockId
        }
    }
    return InterpreterResult(output, variables)
}

fun evalRpn(expr: String, variables: Map<String, String>): String {
    val tokens = expr.trim().split(" ").filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return ""
    val stack = ArrayDeque<Double>()
    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
            variables.containsKey(token) -> stack.addLast(variables[token]?.toDoubleOrNull() ?: 0.0)
            token in setOf("+", "-", "*", "/", "%", "^") -> {
                if (stack.isEmpty()) return "Error"
                val b = stack.removeLast()
                if (stack.isEmpty()) return "Error"
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    "%" -> a % b
                    "^" -> Math.pow(a, b)
                    else -> return "Error"
                }
                stack.addLast(res)
            }
            else -> return "Error"
        }
    }
    return if (stack.size == 1) stack.last().toString() else "Error"
}





