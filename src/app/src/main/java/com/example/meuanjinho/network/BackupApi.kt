package com.example.meuanjinho.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface BackupApi {

    @POST("api/v1/backups")
    suspend fun criarBackup(
        @Body request: CreateBackupRequest
    ): CreateBackupResponse

    @Multipart
    @POST("api/v1/backups/{codigo}/upload")
    suspend fun enviarBackup(
        @Path("codigo") codigo: String,
        @Part("email") email: RequestBody,
        @Part("registrosJson") registrosJson: RequestBody,
        @Part files: List<MultipartBody.Part>
    )
}