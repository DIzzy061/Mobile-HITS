package com.example.codeblockhits.data

data class PrintBlock(
    override val id: Int,
    val expressions: List<String> = listOf(""),
    override var nextBlockId: Int? = null
) : CodeBlock {
    val expression: String
        get() = expressions.joinToString(", ")
} 