package com.example.meuanjinho.data.repository

import com.example.meuanjinho.data.local.dao.CriancaDao
import com.example.meuanjinho.data.local.entity.CriancaEntity

class CriancaRepository(
    private val dao: CriancaDao
) {
    fun getAll() = dao.getAll()

    suspend fun insert(crianca: CriancaEntity) {
        dao.insert(crianca)
    }
}