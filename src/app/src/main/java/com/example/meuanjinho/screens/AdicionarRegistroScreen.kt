package com.example.meuanjinho.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.meuanjinho.R
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.database.Crianca
import com.example.meuanjinho.database.Registro
import com.example.meuanjinho.utils.calcularIdade
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

data class ArquivoSelecionado(
    val caminho: String,
    val nome: String,
    val tamanhoBytes: Long
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarRegistroScreen(
    navController: NavController,
    db: AppDatabase
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    var arquivosSelecionados by remember {
        mutableStateOf<List<ArquivoSelecionado>>(emptyList())
    }

    val caminhos = arquivosSelecionados.joinToString(",") { it.caminho }

    val bannerImage = remember {
        listOf(
            R.drawable.farm,
            R.drawable.sky
        ).random()
    }

    var showPhotoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val descricoes = listOf(
        stringResource(R.string.descricao1),
        stringResource(R.string.descricao2),
        stringResource(R.string.descricao3)
    )

    val placeholderDescricao = remember {
        descricoes.random()
    }

    val hoje = LocalDate.now()

    val criancaDao = db.criancaDao()
    val registroDao = db.registroDao()

    var criancas by remember {
        mutableStateOf<List<Crianca>>(emptyList())
    }

    var criancaSelecionada by remember {
        mutableStateOf<Crianca?>(null)
    }

    var arquivoCameraAtual by remember {
        mutableStateOf<File?>(null)
    }

    var uriCameraAtual by remember {
        mutableStateOf<Uri?>(null)
    }

    LaunchedEffect(Unit) {
        criancas = criancaDao.getAll()
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        val novosArquivos = uris.mapNotNull { uri ->
            copiarImagemDaGaleriaParaApp(context, uri)
        }

        arquivosSelecionados = arquivosSelecionados + novosArquivos
        showPhotoSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        val arquivo = arquivoCameraAtual

        if (sucesso && arquivo != null && arquivo.exists()) {
            arquivosSelecionados = arquivosSelecionados + ArquivoSelecionado(
                caminho = arquivo.absolutePath,
                nome = arquivo.name,
                tamanhoBytes = arquivo.length()
            )
        }

        arquivoCameraAtual = null
        uriCameraAtual = null
        showPhotoSheet = false
    }

    fun abrirCamera() {
        val arquivo = criarArquivoImagem(context)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivo
        )

        arquivoCameraAtual = arquivo
        uriCameraAtual = uri

        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Registro") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24),
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showPhotoSheet = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.photo_camera_24),
                            contentDescription = "Adicionar foto"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Image(
                painter = painterResource(bannerImage),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                placeholder = { Text("Qual será o título da memória?") }
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descrição") },
                placeholder = { Text(placeholderDescricao) },
                minLines = 5
            )

            Text(
                text = "Criança",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                criancas.forEach { crianca ->

                    val icon = if (crianca.sexo == "M") {
                        R.drawable.baby_boy_icon
                    } else {
                        R.drawable.baby_girl_icon
                    }

                    val isSelected = criancaSelecionada == crianca

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                criancaSelecionada = crianca
                            }
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

                            Column {
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

            Text(
                text = "Fotos adicionadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (arquivosSelecionados.isEmpty()) {
                Text(
                    text = "Nenhuma foto adicionada ainda.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    arquivosSelecionados.forEach { arquivo ->
                        ArquivoSelecionadoLinha(
                            arquivo = arquivo
                        )
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        registroDao.insertAll(
                            Registro(
                                titulo = titulo,
                                descricao = descricao,
                                criancaId = criancaSelecionada?.criancaId,
                                dataCriacao = hoje.toString(),
                                arquivos_associados = caminhos
                            )
                        )

                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_24),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Adicionar registro")
            }

            if (showPhotoSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showPhotoSheet = false
                    },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Adicionar foto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PhotoOptionButton(
                                icon = R.drawable.photo_camera_24,
                                label = "Tirar foto",
                                onClick = {
                                    abrirCamera()
                                }
                            )

                            PhotoOptionButton(
                                icon = R.drawable.image_24,
                                label = "Galeria",
                                onClick = {
                                    galeriaLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArquivoSelecionadoLinha(
    arquivo: ArquivoSelecionado
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.image_24),
                contentDescription = "Imagem",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = arquivo.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = arquivo.tamanhoBytes.formatarTamanhoArquivo(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PhotoOptionButton(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun criarArquivoImagem(context: Context): File {
    val timeStamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.getDefault()
    ).format(Date())

    val storageDir = File(context.filesDir, "registros").apply {
        mkdirs()
    }

    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

private fun copiarImagemDaGaleriaParaApp(
    context: Context,
    uri: Uri
): ArquivoSelecionado? {
    return try {
        val nomeOriginal = obterNomeArquivo(context, uri)
            ?: "imagem_${System.currentTimeMillis()}.jpg"

        val arquivoDestino = criarArquivoComNomeSeguro(
            context = context,
            nomeOriginal = nomeOriginal
        )

        context.contentResolver.openInputStream(uri)?.use { input ->
            arquivoDestino.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null

        ArquivoSelecionado(
            caminho = arquivoDestino.absolutePath,
            nome = arquivoDestino.name,
            tamanhoBytes = arquivoDestino.length()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun criarArquivoComNomeSeguro(
    context: Context,
    nomeOriginal: String
): File {
    val storageDir = File(context.filesDir, "registros").apply {
        mkdirs()
    }

    val nomeLimpo = nomeOriginal
        .replace(" ", "_")
        .replace(Regex("[^A-Za-z0-9._-]"), "")

    val timeStamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss_SSS",
        Locale.getDefault()
    ).format(Date())

    return File(
        storageDir,
        "${timeStamp}_$nomeLimpo"
    )
}

private fun obterNomeArquivo(
    context: Context,
    uri: Uri
): String? {
    var nome: String? = null

    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index >= 0) {
                nome = cursor.getString(index)
            }
        }
    }

    return nome
}

private fun Long.formatarTamanhoArquivo(): String {
    if (this <= 0L) return "Tamanho desconhecido"

    val kb = this / 1024.0
    val mb = kb / 1024.0

    return if (mb >= 1) {
        String.format(Locale("pt", "BR"), "%.2f MB", mb)
    } else {
        String.format(Locale("pt", "BR"), "%.1f KB", kb)
    }
}