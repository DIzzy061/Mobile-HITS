package com.example.codeblockhits.data

sealed interface CodeBlock {
    val id: Int
    var nextBlockId: Int?
}

