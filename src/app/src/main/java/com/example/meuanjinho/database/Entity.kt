package com.example.meuanjinho.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class Crianca(
    @PrimaryKey(autoGenerate = true) val criancaId: Int = 0,
    @ColumnInfo(name = "nome") val nome: String,
    @ColumnInfo(name = "sexo") val sexo: String,
    @ColumnInfo(name = "data_nascimento") val dataNascimento: String
)

@Entity
data class Registro(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "titulo") val titulo: String,
    @ColumnInfo(name = "descricao") val descricao: String,
    @ColumnInfo(name = "crianca_id") val criancaId: Int,
    @ColumnInfo(name = "arquivos_associados") val arquivos_associados: String,
    @ColumnInfo(name = "data_criacao") val dataCriacao: String
)

data class CriancaComRegistros(
    @Embedded val crianca: Crianca,
    @Relation(
        parentColumn = "criancaId",
        entityColumn = "crianca_id"
    )
    val registros: List<Registro>
)