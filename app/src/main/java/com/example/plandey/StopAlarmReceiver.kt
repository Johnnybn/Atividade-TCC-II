package com.example.plandey

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private val Nothing?.isPlaying: Any
    get() {
        TODO("Not yet implemented")
    }

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("StopAlarmReceiver", "Parando o alarme")

        // Parar o toque do alarme
        val ringtone = null
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }

        // Cancelar a notificação
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(1) // Cancelar a notificação com ID 1
    }
}

private fun Nothing?.stop() {
    TODO("Not yet implemented")
}
