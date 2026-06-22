package com.example.meuanjinho.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meuanjinho.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController
) {

    val hoje = LocalDate.now()

    val formatter = DateTimeFormatter.ofPattern(
        "EEEE, d 'de' MMMM 'de' yyyy",
        Locale("pt", "BR")
    )

    val dataFormatada = hoje.format(formatter)
        .replaceFirstChar { it.uppercase() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("adicionar_registro")
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_24),
                    contentDescription = "Adicionar Registro hoje"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Text(
                text = dataFormatada,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.height(24.dp))

            // Conteúdo da tela
        }
    }
}