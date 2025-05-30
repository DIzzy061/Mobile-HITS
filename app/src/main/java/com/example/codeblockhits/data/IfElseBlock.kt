package com.example.codeblockhits.data

import com.example.codeblockhits.data.CodeBlock

data class IfElseBlock(
    override val id: Int,
    val leftOperand: String = "",
    val operator: String = "==",
    val rightOperand: String = "",
    val thenBlocks: List<CodeBlock> = emptyList(),
    val elseBlocks: List<CodeBlock> = emptyList(),
    override var nextBlockId: Int? = null
) : CodeBlock 