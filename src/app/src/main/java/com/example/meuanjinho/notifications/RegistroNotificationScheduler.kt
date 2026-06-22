package com.example.meuanjinho.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val REGISTRO_DIARIO_WORK_NAME = "registro_diario_work"

fun agendarNotificacaoDiaria(context: Context) {
    val request = PeriodicWorkRequestBuilder<RegistroNotificationWorker>(
        1,
        TimeUnit.DAYS
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        REGISTRO_DIARIO_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}

fun testarNotificacaoEm10Segundos(context: Context) {
    val request = OneTimeWorkRequestBuilder<RegistroNotificationWorker>()
        .setInitialDelay(10, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(request)
}