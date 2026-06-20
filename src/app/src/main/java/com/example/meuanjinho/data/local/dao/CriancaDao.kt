package com.example.meuanjinho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.meuanjinho.data.local.entity.CriancaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CriancaDao {

    @Insert
    suspend fun insert(crianca: CriancaEntity): Long

    @Query("SELECT * FROM criancas")
    fun getAll(): Flow<List<CriancaEntity>>
}