package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.SalimDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SalimDatabase.getInstance(context)
                val scheduledList = db.scheduledMessageDao().getPendingMessages().firstOrNull() ?: emptyList()
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                for (msg in scheduledList) {
                    val alarmIntent = Intent(context, ScheduledMessageReceiver::class.java).apply {
                        putExtra("scheduled_id", msg.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        msg.id.toInt(),
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            msg.scheduledTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            msg.scheduledTime,
                            pendingIntent
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
