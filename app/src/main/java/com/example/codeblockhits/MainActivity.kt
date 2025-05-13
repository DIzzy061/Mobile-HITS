package com.example.codeblockhits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.codeblockhits.ui.theme.CodeBlockHITSTheme
import com.example.codeblockhits.compose.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeBlockHITSTheme {
                MainScreen()
            }
        }
    }
}
