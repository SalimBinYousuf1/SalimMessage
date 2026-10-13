package com.example

import android.app.Application
import com.example.core.notification.NotificationHelper
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SalimApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)

        // Asynchronously check and sync contacts and messages if permissions exist
        CoroutineScope(Dispatchers.IO).launch {
            val repository = SmsRepository.getInstance(this@SalimApp)
            repository.syncInitialMessages()
        }
    }
}
