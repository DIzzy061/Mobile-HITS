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


