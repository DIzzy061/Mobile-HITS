package com.example.codeblockhits.data

data class VariableBlock(
    override val id: Int,
    val name: String,
    val value: String = "0"
) : CodeBlock
