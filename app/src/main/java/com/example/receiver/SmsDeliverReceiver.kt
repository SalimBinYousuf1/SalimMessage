package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.core.notification.NotificationHelper
import com.example.core.util.OtpDetector
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: return
        val bodyBuilder = StringBuilder()
        var timestamp = System.currentTimeMillis()
        var subId = intent.getIntExtra("subscription", -1)
        if (subId == -1) {
            subId = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1)
        }

        for (msg in messages) {
            bodyBuilder.append(msg.displayMessageBody ?: "")
            timestamp = msg.timestampMillis
        }

        val fullBody = bodyBuilder.toString()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = SmsRepository.getInstance(context)
                val msgId = repository.receiveIncomingMessage(sender, fullBody, timestamp, subId)
                if (msgId > 0) {
                    val otp = OtpDetector.extractOtp(fullBody)
                    val conv = repository.getConversation(msgId)
                    NotificationHelper.showSmsNotification(
                        context = context,
                        sender = conv?.contactName ?: sender,
                        body = fullBody,
                        threadId = conv?.threadId ?: msgId,
                        otpCode = otp
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
