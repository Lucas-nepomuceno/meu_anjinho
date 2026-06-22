package com.example.meuanjinho.network

data class CreateBackupRequest(
    val email: String
)

data class CreateBackupResponse(
    val backupCode: String
)

data class RegistroBackupDto(
    val localId: Int,
    val titulo: String,
    val descricao: String,
    val dataCriacao: String,
    val arquivos: List<String>
)

data class BackupUploadRequest(
    val registros: List<RegistroBackupDto>
)