package com.example.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.telephony.TelephonyManager
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadlessSmsSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && TelephonyManager.ACTION_RESPOND_VIA_MESSAGE == intent.action) {
            val uri: Uri? = intent.data
            val text: String? = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (uri != null && !text.isNullOrBlank()) {
                val recipient = uri.schemeSpecificPart
                val repository = SmsRepository.getInstance(applicationContext)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.sendMessage(recipient, text)
                    } finally {
                        stopSelf(startId)
                    }
                }
                return START_NOT_STICKY
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
