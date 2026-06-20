package com.example.meuanjinho.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Period

@RequiresApi(Build.VERSION_CODES.O)
fun calcularIdade(dataNascimento: String): String {
    val nascimento = LocalDate.parse(dataNascimento)
    val hoje = LocalDate.now()
    val idade = Period.between(nascimento, hoje)

    return when {
        idade.years > 0 -> "${idade.years} anos"
        idade.months > 0 -> "${idade.months} meses"
        else -> "${idade.days} dias"
    }
}