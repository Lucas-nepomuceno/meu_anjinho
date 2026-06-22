package com.example.meuanjinho.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meuanjinho.R
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.database.Registro
import com.example.meuanjinho.network.BackupService
import com.example.meuanjinho.network.RetrofitClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrosScreen(
    navController: NavController,
    db: AppDatabase
) {
    val registroDao = db.registroDao()


    var registros by remember {
        mutableStateOf<List<Registro>>(emptyList())
    }

    LaunchedEffect(Unit) {
        registros = registroDao.getTodosOrdenadosPorData()
    }

    val registrosAgrupados = registros.groupBy { it.dataCriacao }

    var showBackupDialog by remember {
        mutableStateOf(false)
    }

    var emailBackup by remember {
        mutableStateOf("")
    }

    var backupCodeGerado by remember {
        mutableStateOf<String?>(null)
    }

    var backupCarregando by remember {
        mutableStateOf(false)
    }

    var erroBackup by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()
    val backupService = remember {
        BackupService(RetrofitClient.backupApi)
    }

    Scaffold { innerPadding ->

        if (registros.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nenhum registro encontrado.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Seus Registros",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(8.dp))
                }

                registrosAgrupados.forEach { (data, registrosDaData) ->

                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = formatarDataRegistro(data),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    itemsIndexed(registrosDaData) { index, registro ->

                        RegistroListaItem(
                            registro = registro,
                            onClick = {
                                navController.navigate("detalhe_registro/${registro.uid}")
                            }
                        )

                        if (index < registrosDaData.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 44.dp)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp)) // Espaço entre o último registro e o botão
                    Button(
                        onClick = {
                            showBackupDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salvar backup online")
                    }
                }
            }

            if (showBackupDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!backupCarregando) {
                            showBackupDialog = false
                        }
                    },
                    title = {
                        Text("Salvar backup online")
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Informe seu email. Vamos gerar um código único para você recuperar seus registros depois."
                            )

                            OutlinedTextField(
                                value = emailBackup,
                                onValueChange = { emailBackup = it },
                                label = { Text("Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            erroBackup?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            backupCodeGerado?.let { codigo ->
                                Text(
                                    text = "Código do backup: $codigo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Guarde esse código. Ele será necessário para restaurar seus registros."
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            enabled = !backupCarregando && backupCodeGerado == null,
                            onClick = {
                                if (emailBackup.isBlank()) {
                                    erroBackup = "Informe um email válido."
                                    return@Button
                                }

                                scope.launch {
                                    backupCarregando = true
                                    erroBackup = null

                                    try {
                                        val registros = registroDao.getTodosOrdenadosPorData()

                                        val codigo = backupService.salvarBackupOnline(
                                            email = emailBackup.trim(),
                                            registros = registros
                                        )

                                        backupCodeGerado = codigo
                                    } catch (e: Exception) {
                                        erroBackup = e.message ?: "Erro ao salvar backup."
                                    } finally {
                                        backupCarregando = false
                                    }
                                }
                            }
                        ) {
                            Text(
                                if (backupCarregando) {
                                    "Salvando..."
                                } else if (backupCodeGerado == null) {
                                    "Salvar"
                                } else {
                                    "Salvo"
                                }
                            )
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            enabled = !backupCarregando,
                            onClick = {
                                showBackupDialog = false
                                backupCodeGerado = null
                                erroBackup = null
                            }
                        ) {
                            Text("Fechar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RegistroListaItem(
    registro: Registro,
    onClick: () -> Unit
) {
    val temFotos = !registro.arquivos_associados.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(
                if (temFotos) R.drawable.image_24 else R.drawable.description_24
            ),
            contentDescription = "Registro",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = registro.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = registro.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (temFotos) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Com foto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatarDataRegistro(data: String): String {
    return try {
        val localDate = LocalDate.parse(data)

        val hoje = LocalDate.now()
        val ontem = hoje.minusDays(1)

        when (localDate) {
            hoje -> "Hoje"
            ontem -> "Ontem"
            else -> {
                val formatter = DateTimeFormatter.ofPattern(
                    "d 'de' MMMM 'de' yyyy",
                    Locale("pt", "BR")
                )

                localDate.format(formatter)
                    .replaceFirstChar { it.uppercase() }
            }
        }
    } catch (e: Exception) {
        data
    }
}