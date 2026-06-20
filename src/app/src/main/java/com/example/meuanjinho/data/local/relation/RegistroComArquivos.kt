package com.example.meuanjinho.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.meuanjinho.data.local.entity.ArquivoEntity
import com.example.meuanjinho.data.local.entity.RegistroEntity

data class RegistroComArquivos(
    @Embedded val registro: RegistroEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "registroId"
    )
    val arquivos: List<ArquivoEntity>
)