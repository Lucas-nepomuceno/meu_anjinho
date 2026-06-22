package com.example.meuanjinho.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.meuanjinho.MainActivity
import com.example.meuanjinho.R

class RegistroNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        criarCanalNotificacao(context)
        mostrarNotificacaoRegistro(context)

        return Result.success()
    }
}

private const val CHANNEL_ID = "registro_diario_channel"
private const val NOTIFICATION_ID = 2001

fun mostrarNotificacaoRegistro(context: Context) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val mensagens = listOf(
        "Nenhuma conquista para escrever sobre hoje... Duvido!",
        "Registre o dia do seu anjinho, agora!",
        "Seu anjinho fez algo incrível hoje? Não esqueça de registrar"
    )

    val mensagem = mensagens.random()

    val registrarIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("destino", "adicionar_registro")
    }

    val registrarPendingIntent = PendingIntent.getActivity(
        context,
        1001,
        registrarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val lembrarIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotificationActionReceiver.ACTION_LEMBRAR_DEPOIS
    }

    val lembrarPendingIntent = PendingIntent.getBroadcast(
        context,
        1002,
        lembrarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.outline_child_hat_24)
        .setContentTitle("Meu Anjinho")
        .setContentText(mensagem)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(mensagem)
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(registrarPendingIntent)
        .setAutoCancel(true)
        .addAction(
            R.drawable.alarm_24,
            "Lembre-me",
            lembrarPendingIntent
        )
        .addAction(
            R.drawable.add_24,
            "Registrar",
            registrarPendingIntent
        )
        .build()

    NotificationManagerCompat.from(context).notify(
        NOTIFICATION_ID,
        notification
    )
}

fun criarCanalNotificacao(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Registro diário",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Lembretes para registrar o dia da criança"
        }

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }
}