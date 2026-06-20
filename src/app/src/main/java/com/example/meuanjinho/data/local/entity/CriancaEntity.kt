package com.example.meuanjinho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "criancas")
data class CriancaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val sexo: String,
    val dataNascimento: String
)