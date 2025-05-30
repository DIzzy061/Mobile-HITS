package com.example.codeblockhits.data


data class WhileBlock(
    override val id: Int,
    val leftOperand: String = "",
    val operator: String = "!=",
    val rightOperand: String = "0",
    val innerBlocks: List<CodeBlock> = emptyList(),
    override var nextBlockId: Int? = null
) : CodeBlock 