package com.example.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R
import com.example.receiver.NotificationActionReceiver

object NotificationHelper {
    const val CHANNEL_ID_SMS = "salim_sms_channel"
    const val KEY_TEXT_REPLY = "key_text_reply"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_SMS,
                context.getString(R.string.channel_sms),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_sms_desc)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showSmsNotification(
        context: Context,
        sender: String,
        body: String,
        threadId: Long,
        otpCode: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tap opens conversation in MainActivity
        val contentIntent = PendingIntent.getActivity(
            context,
            threadId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("thread_id", threadId)
                putExtra("address", sender)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Inline reply
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.enter_text))
            .build()

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            (threadId * 10 + 1).toInt(),
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_REPLY
                putExtra("thread_id", threadId)
                putExtra("address", sender)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            context.getString(R.string.notification_reply),
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Mark as read
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            (threadId * 10 + 2).toInt(),
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_READ
                putExtra("thread_id", threadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            context.getString(R.string.mark_read),
            markReadPendingIntent
        ).build()

        val person = Person.Builder()
            .setName(sender)
            .setKey(sender)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .addMessage(body, System.currentTimeMillis(), person)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID_SMS)
            .setSmallIcon(R.drawable.ic_salim_launcher_1791905695962)
            .setStyle(messagingStyle)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .addAction(replyAction)
            .addAction(markReadAction)

        // If OTP detected, add quick Copy Code action
        if (!otpCode.isNullOrBlank()) {
            val copyCodePendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId * 10 + 3).toInt(),
                Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_COPY_OTP
                    putExtra("otp_code", otpCode)
                    putExtra("notification_id", threadId.toInt())
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val copyAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_save,
                "${context.getString(R.string.copy_code)}: $otpCode",
                copyCodePendingIntent
            ).build()
            notificationBuilder.addAction(copyAction)
        }

        notificationManager.notify(threadId.toInt(), notificationBuilder.build())
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}
