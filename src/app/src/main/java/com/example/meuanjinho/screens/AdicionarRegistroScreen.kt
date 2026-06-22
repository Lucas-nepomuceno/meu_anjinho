package com.example.meuanjinho.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meuanjinho.R
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.database.Crianca
import com.example.meuanjinho.database.Registro
import com.example.meuanjinho.utils.calcularIdade
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarRegistroScreen(
    navController: NavController,
    db: AppDatabase
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var caminhos by remember { mutableStateOf("") }


    val bannerImage = remember {
        listOf(
            R.drawable.farm,
            R.drawable.sky
        ).random()
    }

    // Estados para controlar o BottomSheet da foto
    var showPhotoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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

    var criancas by remember {
        mutableStateOf<List<Crianca>>(emptyList())
    }

    LaunchedEffect(Unit) {
        criancas = criancaDao.getAll()
    }

    val registroDao = db.registroDao()


    var criancaSelecionada by remember { mutableStateOf<Crianca?>(null) }

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
                    IconButton(onClick = { showPhotoSheet = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.photo_camera_24), // Certifique-se de ter este ícone
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

                    val isSelected = criancaSelecionada == crianca

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
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
                    }
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
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
                    onDismissRequest = { showPhotoSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 32.dp), // Espaço extra na parte de baixo
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Adicionar Foto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // OPÇÃO 1: CÂMERA
                            YourPhotoScreen(
                                caminhoFotoTirada = caminhos,
                                onAlterarCaminhoFotoTirada = {caminhos = it}
                            )

                            // OPÇÃO 2: GALERIA
                            PhotoOptionButton(
                                icon = R.drawable.image_24,
                                label = "Galeria",
                                onClick = {
                                    // TODO: Lógica para abrir o seletor de Galeria
                                    showPhotoSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Componente auxiliar para os botões dentro do BottomSheet
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
            modifier = Modifier.fillMaxSize().padding(16.dp),
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

@Composable
fun YourPhotoScreen(
    caminhoFotoTirada: String,
    onAlterarCaminhoFotoTirada: (String) -> Unit
) {
    val context = LocalContext.current

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var showPhotoSheet by remember { mutableStateOf(true) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            val absolutePath = photoFile!!.absolutePath
            if (caminhoFotoTirada == "") onAlterarCaminhoFotoTirada(absolutePath) else onAlterarCaminhoFotoTirada(
                "$caminhoFotoTirada,$absolutePath"
            )

            println("Foto salva com sucesso em: $absolutePath")
        } else {
            println("Usuário cancelou ou falhou ao tirar a foto.")
        }
    }

    // Função auxiliar para criar o arquivo no appDirectory e disparar a câmera
    fun launchCamera() {
        val file = createImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        photoFile = file
        photoUri = uri

        takePictureLauncher.launch(uri)
    }

    PhotoOptionButton(
        icon = R.drawable.photo_camera_24,
        label = "Tirar Foto",
        onClick = {
            launchCamera()
            showPhotoSheet = false
        }
    )
}

/**
 * Cria um arquivo de imagem vazio dentro do diretório interno do App (appDirectory).
 * Os arquivos aqui são privados do app e são apagados se o app for desinstalado.
 */
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"

    // context.filesDir aponta direto para o appDirectory interno seguro
    val storageDir = context.filesDir

    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg",        /* suffix */
        storageDir     /* directory */
    )
}
