package com.example.codeblockhits.data

sealed interface CodeBlock {
    val id: Int
    var nextBlockId: Int?
}
data class VariableBlock(
    override val id: Int,
    val name: String,
    val value: String,
    override var nextBlockId: Int? = null
) : CodeBlock

data class AssignmentBlock(
    override val id: Int,
    val target: String,
    val expression: String,
    override var nextBlockId: Int? = null
) : CodeBlock

data class IfElseBlock(
    override val id: Int,
    val leftOperand: String = "",
    val operator: String = "==",
    val rightOperand: String = "",
    val thenBlocks: List<CodeBlock> = emptyList(),
    val elseBlocks: List<CodeBlock> = emptyList(),
    override var nextBlockId: Int? = null
) : CodeBlock

data class PrintBlock(
    override val id: Int,
    val expressions: List<String> = listOf(""),
    override var nextBlockId: Int? = null
) : CodeBlock {
    val expression: String
        get() = expressions.joinToString(", ")
}

data class WhileBlock(
    override val id: Int,
    val leftOperand: String = "",
    val operator: String = "!=",
    val rightOperand: String = "0",
    val innerBlocks: List<CodeBlock> = emptyList(),
    override var nextBlockId: Int? = null
) : CodeBlock

