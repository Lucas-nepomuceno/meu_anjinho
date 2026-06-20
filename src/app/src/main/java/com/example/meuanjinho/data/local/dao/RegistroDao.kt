package com.example.meuanjinho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.meuanjinho.data.local.entity.RegistroEntity

@Dao
interface RegistroDao {

    @Insert
    suspend fun insert(registro: RegistroEntity): Long

    @Query("SELECT * FROM registros WHERE criancaId = :criancaId")
    suspend fun getByCrianca(criancaId: Long): List<RegistroEntity>
}