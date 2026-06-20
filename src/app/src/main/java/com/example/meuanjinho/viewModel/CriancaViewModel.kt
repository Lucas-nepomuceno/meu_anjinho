package com.example.meuanjinho.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meuanjinho.data.local.entity.CriancaEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.meuanjinho.data.repository.CriancaRepository
import kotlinx.coroutines.flow.StateFlow

class CriancaViewModel(
    private val repository: CriancaRepository
) : ViewModel() {

    val criancas: StateFlow<List<CriancaEntity>> = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    fun addCrianca(crianca: CriancaEntity) {
        viewModelScope.launch {
            repository.insert(crianca)
        }
    }
}