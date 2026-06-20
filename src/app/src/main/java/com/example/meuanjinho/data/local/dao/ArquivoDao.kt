package com.example.meuanjinho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.meuanjinho.data.local.entity.ArquivoEntity

@Dao
interface ArquivoDao {

    @Insert
    suspend fun insertAll(arquivos: List<ArquivoEntity>)

    @Query("SELECT * FROM arquivos WHERE registroId = :registroId")
    suspend fun getByRegistro(registroId: Long): List<ArquivoEntity>
}