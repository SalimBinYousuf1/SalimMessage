package com.example.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.core.model.MessageStatus
import com.example.data.local.SalimDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsStatusReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SMS_SENT = "com.salim.sms.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.salim.sms.ACTION_SMS_DELIVERED"
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_PART_INDEX = "extra_part_index"
        const val EXTRA_TOTAL_PARTS = "extra_total_parts"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        if (messageId == -1L) return

        val resultCode = resultCode
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SalimDatabase.getInstance(context)
                val messageDao = db.messageDao()

                when (intent.action) {
                    ACTION_SMS_SENT -> {
                        if (resultCode == Activity.RESULT_OK) {
                            messageDao.updateStatus(messageId, MessageStatus.SENT.name)
                        } else {
                            messageDao.updateStatus(messageId, MessageStatus.FAILED.name)
                        }
                    }
                    ACTION_SMS_DELIVERED -> {
                        if (resultCode == Activity.RESULT_OK) {
                            messageDao.updateStatus(messageId, MessageStatus.DELIVERED.name)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
