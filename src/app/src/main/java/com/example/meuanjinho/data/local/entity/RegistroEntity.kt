package com.example.meuanjinho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "registros",
    foreignKeys = [
        ForeignKey(
            entity = CriancaEntity::class,
            parentColumns = ["id"],
            childColumns = ["criancaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val titulo: String,
    val descricao: String,

    val criancaId: Long
)