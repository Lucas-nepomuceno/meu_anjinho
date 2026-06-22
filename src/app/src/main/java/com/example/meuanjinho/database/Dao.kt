package com.example.meuanjinho.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CriancaDao {
    @Query("SELECT * FROM crianca")
    suspend fun getAll(): List<Crianca>

    @Query("SELECT * FROM crianca WHERE criancaId IN (:criancaIds)")
    suspend fun loadAllByIds(criancaIds: IntArray): List<Crianca>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg criancas: Crianca)

    @Delete
    suspend fun delete(crianca: Crianca)
}


@Dao
interface RegistroDao {
    @Query("SELECT * FROM registro")
    suspend fun getAll(): List<Registro>

    @Query("SELECT * FROM registro WHERE data_criacao = :data")
    suspend fun getRegistrosDoDia(data: String): List<Registro>

    @Query("SELECT * FROM registro WHERE uid IN (:registroIds)")
    suspend fun loadAllByIds(registroIds: IntArray): List<Registro>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg registros: Registro)

    @Delete
    suspend fun delete(registro: Registro)
}