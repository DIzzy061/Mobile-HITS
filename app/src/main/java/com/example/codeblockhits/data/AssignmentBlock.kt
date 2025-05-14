package com.example.codeblockhits.data

data class AssignmentBlock(
    override val id: Int,
    val target: String = "",
    val expression: String = ""
): CodeBlock