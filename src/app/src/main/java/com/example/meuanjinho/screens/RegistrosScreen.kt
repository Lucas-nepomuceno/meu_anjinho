package com.example.meuanjinho.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.database.Crianca
import com.example.meuanjinho.database.Registro

@Composable
fun RegistrosScreen(
    db: AppDatabase
) {

    val registroDao = db.registroDao()

    var registros by remember {
        mutableStateOf<List<Registro>>(emptyList())
    }

    LaunchedEffect(Unit) {
        registros = registroDao.getAll()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(registros) {registro ->
                Text(registro.titulo)
            }
        }
        Button(
            onClick = {
                // TODO: salvar backup online
            }
        ) {
            Text("Salvar backup online")
        }
    }
}