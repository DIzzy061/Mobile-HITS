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

fun evaluateIfElseBlock(
    block: IfElseBlock,
    variables: Map<String, String>
): IfElseBlock {
    val left = evaluateExpression(block.leftOperand, variables).toDoubleOrNull() ?: return block
    val right = evaluateExpression(block.rightOperand, variables).toDoubleOrNull() ?: return block

    val condition = when (block.operator) {
        "==" -> left == right
        "!=" -> left != right
        ">" -> left > right
        "<" -> left < right
        ">=" -> left >= right
        "<=" -> left <= right
        else -> false
    }

    val evaluatedThen = block.thenBlocks.map { evaluateBlock(it, variables) }
    val evaluatedElse = block.elseBlocks.map { evaluateBlock(it, variables) }

    return block.copy(
        thenBlocks = if (condition) evaluatedThen else block.thenBlocks,
        elseBlocks = if (!condition) evaluatedElse else block.elseBlocks
    )
}

fun evaluateBlock(block: CodeBlock, variables: Map<String, String>): CodeBlock {
    return when (block) {
        is VariableBlock -> block.copy(value = evaluateExpression(block.value, variables))
        is IfElseBlock -> evaluateIfElseBlock(block, variables)
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
            if (pos < expression.length) throw RuntimeException("Unexpected: " + ch)
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    eat('%') -> x %= parseFactor()
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
                throw RuntimeException("Unexpected: " + ch)
            }

            if (eat('^')) x = Math.pow(x, parseFactor())

            return x
        }
    }.parse()
}
