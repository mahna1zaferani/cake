package com.gulserakh.sweets.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.gulserakh.sweets.SweetsApplication

class LoginActivity : ComponentActivity() {

    private val viewModel = LoginViewModel((applicationContext as SweetsApplication).database)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LoginScreen(viewModel)
            }
        }
    }
}
