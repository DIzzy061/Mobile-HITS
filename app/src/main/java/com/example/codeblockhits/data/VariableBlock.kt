package com.example.codeblockhits.data

data class VariableBlock(
    override val id: Int,
    val name: String,
    val value: String,
    override var nextBlockId: Int? = null
) : CodeBlock 