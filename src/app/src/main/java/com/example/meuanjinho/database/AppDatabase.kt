package com.example.meuanjinho.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Crianca::class, Registro::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun criancaDao(): CriancaDao
    abstract fun registroDao(): RegistroDao
}