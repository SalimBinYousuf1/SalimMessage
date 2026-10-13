package com.example.receiver

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.example.R
import com.example.core.notification.NotificationHelper
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REPLY = "com.salim.sms.ACTION_REPLY"
        const val ACTION_MARK_READ = "com.salim.sms.ACTION_MARK_READ"
        const val ACTION_COPY_OTP = "com.salim.sms.ACTION_COPY_OTP"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val repository = SmsRepository.getInstance(context)

        when (intent.action) {
            ACTION_REPLY -> {
                val threadId = intent.getLongExtra("thread_id", -1L)
                val address = intent.getStringExtra("address") ?: return
                val bundle = RemoteInput.getResultsFromIntent(intent)
                val replyText = bundle?.getCharSequence(NotificationHelper.KEY_TEXT_REPLY)?.toString() ?: return

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.sendMessage(address, replyText)
                        if (threadId != -1L) {
                            repository.markThreadAsRead(threadId)
                        }
                        NotificationHelper.dismissNotification(context, threadId.toInt())
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_MARK_READ -> {
                val threadId = intent.getLongExtra("thread_id", -1L)
                if (threadId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.markThreadAsRead(threadId)
                            NotificationHelper.dismissNotification(context, threadId.toInt())
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_COPY_OTP -> {
                val otpCode = intent.getStringExtra("otp_code") ?: return
                val notificationId = intent.getIntExtra("notification_id", -1)

                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("OTP Code", otpCode)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                if (notificationId != -1) {
                    NotificationHelper.dismissNotification(context, notificationId)
                }
            }
        }
    }
}
