package com.efom.randomlearn.utils

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.efom.randomlearn.R

class NotificationService : IntentService("NotificationService") {
    companion object {
        const val MY_CHANNEL_ID = "myChannel"
    }

    override fun onHandleIntent(intent: Intent?) {
        var builder = NotificationCompat.Builder(this, MY_CHANNEL_ID)
            .setContentTitle("A repasar")
            .setContentText("Tienes mucho por repasar")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder)
        }

    }
}