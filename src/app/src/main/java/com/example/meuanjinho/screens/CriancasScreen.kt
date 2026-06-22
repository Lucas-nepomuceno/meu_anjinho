package com.example.meuanjinho.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import com.example.meuanjinho.R
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.database.Crianca
import com.example.meuanjinho.database.CriancaDao
import com.example.meuanjinho.utils.calcularIdade
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriancasScreen(
    db: AppDatabase,
) {
    val criancaDao = db.criancaDao()

    var criancas by remember {
        mutableStateOf<List<Crianca>>(emptyList())
    }

    var mostrarBottomSheet by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        criancas = criancaDao.getAll()
    }

    val scope = rememberCoroutineScope()


    if (mostrarBottomSheet) {
        AdicionarCriancaBottomSheet(
            criancaDao = criancaDao,
            onDismiss = {
                mostrarBottomSheet = false
            },
            onCriancaSalva = {
                scope.launch {
                    criancas = criancaDao.getAll()
                }
            }
        )
    }

    if (criancas.isEmpty()) {
        SemCrianca(
            onAdicionarCrianca = {
                mostrarBottomSheet = true
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarBottomSheet = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_24),
                        contentDescription = "Adicionar Anjinho"
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Seus Anjinhos",
                    style = MaterialTheme.typography.headlineMedium
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(criancas) { crianca ->

                        val icon = if (crianca.sexo == "M") {
                            R.drawable.baby_boy_icon
                        } else {
                            R.drawable.baby_girl_icon
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Image(
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )

                                Column() {
                                    Text(
                                        text = crianca.nome,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = calcularIdade(crianca.dataNascimento)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SemCrianca(
    onAdicionarCrianca: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Você não adicionou nenhum anjinho",
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.titleMedium.fontSize
        )

        Button(
            onClick = onAdicionarCrianca
        ) {
            Text(
                "Clique aqui para adicionar uma criança",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarCriancaBottomSheet(
    criancaDao: CriancaDao,
    onDismiss: () -> Unit,
    onCriancaSalva: () -> Unit
) {
    var nome by remember {
        mutableStateOf("")
    }
    var masculino by remember {
        mutableStateOf(false)
    }
    var dataNascimento by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Adicionar Criança")

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") }
            )

            DatePickerCampoNascimento(
                dataSelecionada = dataNascimento,
                onDataAlterada = { dataNascimento = it }
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Menino?"
                )
                Checkbox(
                    checked = masculino,
                    onCheckedChange = { masculino = it }
                )
            }

            Button(
                onClick = {
                    scope.launch {

                        criancaDao.insertAll(
                            Crianca(
                                nome = nome,
                                sexo = if (masculino) "M" else "F",
                                dataNascimento = dataNascimento
                            )
                        )

                        onCriancaSalva()
                        onDismiss()
                    }
                }
            ) {
                Text("Salvar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerCampoNascimento(
    dataSelecionada: String,
    onDataAlterada: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            }
        }
    )

    // Ajustado com os modificadores corretos para virar um botão de data
    OutlinedTextField(
        value = dataSelecionada,
        onValueChange = { },
        label = { Text("Data de Nascimento") },
        placeholder = { Text("Selecione a data") },
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.calendar_month_24),
                contentDescription = "Selecionar data de nascimento"
            )
        },
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showDatePicker = true
            },
        readOnly = true,
        enabled = false
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        onDataAlterada(formatter.format(Date(selectedDateMillis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
