package com.efom.randomlearn.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val MY_CHANNEL_ID = "myChannel"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val notificationId = intent.getIntExtra("notificationId", 0) // Obtén el ID de notificación
            mNotify(context, notificationId)
        }
    }

    private fun mNotify(context: Context, id: Int){
        val builder = NotificationCompat.Builder(context, MY_CHANNEL_ID)
            .setContentTitle("Preguntas y respuestas")
            .setContentText("Hora de estudiar")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder)
    }
}