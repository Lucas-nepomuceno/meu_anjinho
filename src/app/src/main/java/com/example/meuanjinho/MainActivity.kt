package com.example.meuanjinho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.meuanjinho.navigation.AppNavigation
import com.meu_anjinho.ui.theme.MeuAnjinhoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MeuAnjinhoTheme {
                AppNavigation()
            }
        }
    }
}