package com.example.meuanjinho

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.navigation.AppNavigation
import com.meu_anjinho.ui.theme.MeuAnjinhoTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db: AppDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "meu_anjinho"
        ).build()

        setContent {
            MeuAnjinhoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(db = db)
                }
            }
        }
    }
}