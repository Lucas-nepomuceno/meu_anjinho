package com.example.meuanjinho.network

import com.example.meuanjinho.database.Registro
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class BackupService(
    private val api: BackupApi
) {
    suspend fun salvarBackupOnline(
        email: String,
        registros: List<Registro>
    ): String {
        val createResponse = api.criarBackup(
            CreateBackupRequest(email = email)
        )

        val backupCode = createResponse.backupCode

        val registrosDto = registros.map { registro ->
            val arquivos = registro.arquivos_associados
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            RegistroBackupDto(
                localId = registro.uid,
                titulo = registro.titulo,
                descricao = registro.descricao,
                dataCriacao = registro.dataCriacao,
                arquivos = arquivos
            )
        }

        val backupUpload = BackupUploadRequest(
            registros = registrosDto
        )

        val registrosJson = Gson().toJson(backupUpload)

        val emailBody = email.toRequestBody("text/plain".toMediaType())
        val registrosJsonBody = registrosJson.toRequestBody("text/plain".toMediaType())

        val files = registrosDto.flatMap { registro ->
            registro.arquivos.mapNotNull { caminho ->
                val file = File(caminho)

                if (!file.exists()) {
                    null
                } else {
                    val requestFile = file.asRequestBody("image/*".toMediaType())

                    MultipartBody.Part.createFormData(
                        name = "files",
                        filename = file.name,
                        body = requestFile
                    )
                }
            }
        }

        api.enviarBackup(
            codigo = backupCode,
            email = emailBody,
            registrosJson = registrosJsonBody,
            files = files
        )

        return backupCode
    }
}