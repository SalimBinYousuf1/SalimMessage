package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION) return
        // MMS WAP Push notification received
        val pushData = intent.getByteArrayExtra("data") ?: return
        // In default SMS app, process M-Notification.ind
    }
}
