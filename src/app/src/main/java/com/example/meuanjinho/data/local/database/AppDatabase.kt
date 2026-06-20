package com.example.meuanjinho.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.meuanjinho.data.local.dao.ArquivoDao
import com.example.meuanjinho.data.local.dao.CriancaDao
import com.example.meuanjinho.data.local.dao.RegistroDao
import com.example.meuanjinho.data.local.entity.RegistroEntity
import com.example.meuanjinho.data.local.entity.CriancaEntity
import com.example.meuanjinho.data.local.entity.ArquivoEntity

@Database(
    entities = [
        CriancaEntity::class,
        RegistroEntity::class,
        ArquivoEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun criancaDao(): CriancaDao
    abstract fun registroDao(): RegistroDao
    abstract fun arquivoDao(): ArquivoDao
}