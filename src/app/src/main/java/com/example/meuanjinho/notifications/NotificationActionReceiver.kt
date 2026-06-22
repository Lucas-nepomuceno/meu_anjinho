package com.example.meuanjinho.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        when (intent.action) {
            ACTION_LEMBRAR_DEPOIS -> {
                agendarLembreteDaquiSeisHoras(context)
            }
        }
    }

    companion object {
        const val ACTION_LEMBRAR_DEPOIS =
            "com.example.meuanjinho.ACTION_LEMBRAR_DEPOIS"
    }
}

private fun agendarLembreteDaquiSeisHoras(context: Context) {
    val request = OneTimeWorkRequestBuilder<RegistroNotificationWorker>()
        .setInitialDelay(6, TimeUnit.HOURS)
        .build()

    WorkManager.getInstance(context).enqueue(request)
}