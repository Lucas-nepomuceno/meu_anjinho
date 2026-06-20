package com.example.meuanjinho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "arquivos",
    foreignKeys = [
        ForeignKey(
            entity = RegistroEntity::class,
            parentColumns = ["id"],
            childColumns = ["registroId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ArquivoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val registroId: Long,
    val path: String
)