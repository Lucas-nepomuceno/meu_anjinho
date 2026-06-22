package com.example.meuanjinho

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.navigation.AppNavigation
import com.example.meuanjinho.notifications.agendarNotificacaoDiaria
import com.example.meuanjinho.notifications.testarNotificacaoEm10Segundos
import com.meu_anjinho.ui.theme.MeuAnjinhoTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        agendarNotificacaoDiaria(this)
        testarNotificacaoEm10Segundos(this)


        val db: AppDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "meu_anjinho"
        ).build()

        setContent {
            MeuAnjinhoTheme {
                val destinoInicial = intent?.getStringExtra("destino")
                val navController = rememberNavController()

                SolicitarPermissaoNotificacao()

                LaunchedEffect(destinoInicial) {
                    if (destinoInicial == "adicionar_registro") {
                        navController.navigate("adicionar_registro")
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(db = db, navController= navController)
                }
            }
        }
    }
}

@Composable
fun SolicitarPermissaoNotificacao() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}