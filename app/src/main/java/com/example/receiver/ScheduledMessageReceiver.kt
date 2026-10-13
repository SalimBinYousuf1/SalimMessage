package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.SalimDatabase
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SalimDatabase.getInstance(context)
                val scheduledDao = db.scheduledMessageDao()
                val repository = SmsRepository.getInstance(context)

                val now = System.currentTimeMillis()
                val dueList = scheduledDao.getDueMessages(now)

                for (msg in dueList) {
                    repository.sendMessage(
                        recipient = msg.recipient,
                        body = msg.body,
                        subId = msg.subscriptionId
                    )
                    scheduledDao.markSent(msg.id)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
