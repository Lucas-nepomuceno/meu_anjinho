package com.example.meuanjinho.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.meuanjinho.data.local.entity.CriancaEntity
import com.example.meuanjinho.data.local.entity.RegistroEntity

data class CriancaComRegistros(
    @Embedded val crianca: CriancaEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "criancaId"
    )
    val registros: List<RegistroEntity>
)