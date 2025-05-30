package com.example.codeblockhits.data

data class AssignmentBlock(
    override val id: Int,
    val target: String,
    val expression: String,
    override var nextBlockId: Int? = null
) : CodeBlock 