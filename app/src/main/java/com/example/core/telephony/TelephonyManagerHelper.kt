package com.example.core.telephony

import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.example.core.model.Contact
import com.example.core.model.SimInfo
import com.example.receiver.SmsStatusReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TelephonyManagerHelper(private val context: Context) {

    fun isDefaultSmsApp(): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }

    fun createDefaultSmsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
                ?: Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                }
        } else {
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
        }
    }

    fun hasSmsPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val sendSms = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        return readSms && sendSms
    }

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun getActiveSimCards(): List<SimInfo> {
        val simList = mutableListOf<SimInfo>()
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return simList
        }
        try {
            val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val activeList = subManager?.activeSubscriptionInfoList ?: emptyList()
            for (info in activeList) {
                simList.add(
                    SimInfo(
                        subscriptionId = info.subscriptionId,
                        slotIndex = info.simSlotIndex,
                        displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                        carrierName = info.carrierName?.toString() ?: "Carrier",
                        number = info.number
                    )
                )
            }
        } catch (_: Exception) {}
        return simList
    }

    private fun getSmsManagerForSubId(subId: Int): SmsManager {
        return if (subId >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
        } else if (subId >= 0) {
            @Suppress("DEPRECATION")
            SmsManager.getSmsManagerForSubscriptionId(subId)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    fun sendTextMessage(
        recipient: String,
        text: String,
        subId: Int,
        messageLocalId: Long
    ) {
        val smsManager = getSmsManagerForSubId(subId)
        val parts = smsManager.divideMessage(text)

        val sentIntents = ArrayList<PendingIntent>()
        val deliveryIntents = ArrayList<PendingIntent>()

        for (i in parts.indices) {
            val sentIntent = PendingIntent.getBroadcast(
                context,
                (messageLocalId * 100 + i).toInt(),
                Intent(context, SmsStatusReceiver::class.java).apply {
                    action = SmsStatusReceiver.ACTION_SMS_SENT
                    putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, messageLocalId)
                    putExtra(SmsStatusReceiver.EXTRA_PART_INDEX, i)
                    putExtra(SmsStatusReceiver.EXTRA_TOTAL_PARTS, parts.size)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val deliveryIntent = PendingIntent.getBroadcast(
                context,
                (messageLocalId * 100 + i + 50).toInt(),
                Intent(context, SmsStatusReceiver::class.java).apply {
                    action = SmsStatusReceiver.ACTION_SMS_DELIVERED
                    putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, messageLocalId)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            sentIntents.add(sentIntent)
            deliveryIntents.add(deliveryIntent)
        }

        if (parts.size > 1) {
            smsManager.sendMultipartTextMessage(recipient, null, parts, sentIntents, deliveryIntents)
        } else {
            smsManager.sendTextMessage(recipient, null, text, sentIntents.firstOrNull(), deliveryIntents.firstOrNull())
        }

        // Write to system provider if default SMS app
        if (isDefaultSmsApp()) {
            writeSentSmsToProvider(recipient, text, subId)
        }
    }

    private fun writeSentSmsToProvider(recipient: String, body: String, subId: Int) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.Sent.ADDRESS, recipient)
                put(Telephony.Sms.Sent.BODY, body)
                put(Telephony.Sms.Sent.DATE, System.currentTimeMillis())
                put(Telephony.Sms.Sent.READ, 1)
                put(Telephony.Sms.Sent.TYPE, Telephony.Sms.Sent.MESSAGE_TYPE_SENT)
                if (subId >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    put(Telephony.Sms.Sent.SUBSCRIPTION_ID, subId)
                }
            }
            context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
        } catch (_: Exception) {}
    }

    fun writeIncomingSmsToProvider(sender: String, body: String, timestamp: Long, subId: Int) {
        if (!isDefaultSmsApp()) return
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.Inbox.ADDRESS, sender)
                put(Telephony.Sms.Inbox.BODY, body)
                put(Telephony.Sms.Inbox.DATE, timestamp)
                put(Telephony.Sms.Inbox.READ, 0)
                put(Telephony.Sms.Inbox.TYPE, Telephony.Sms.Inbox.MESSAGE_TYPE_INBOX)
                if (subId >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    put(Telephony.Sms.Inbox.SUBSCRIPTION_ID, subId)
                }
            }
            context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
        } catch (_: Exception) {}
    }

    suspend fun queryDeviceContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        if (!hasContactsPermission()) return@withContext contacts

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val lookupIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    val id = it.getString(idIdx) ?: ""
                    val lookup = it.getString(lookupIdx) ?: ""
                    val name = it.getString(nameIdx) ?: "Unknown"
                    val number = it.getString(numberIdx) ?: ""
                    val photo = if (photoIdx != -1) it.getString(photoIdx) else null
                    if (number.isNotBlank()) {
                        contacts.add(Contact(id, lookup, name, number, photo))
                    }
                }
            }
        } catch (_: Exception) {}
        contacts
    }

    suspend fun importExistingSms(): List<Pair<Long, com.example.core.model.Message>> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<Pair<Long, com.example.core.model.Message>>()
        if (!hasSmsPermissions()) return@withContext messages

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 500"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex(Telephony.Sms._ID)
                val threadIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addrIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
                val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                while (it.moveToNext()) {
                    val id = it.getLong(idIdx)
                    val threadId = it.getLong(threadIdx)
                    val address = it.getString(addrIdx) ?: ""
                    val body = it.getString(bodyIdx) ?: ""
                    val date = it.getLong(dateIdx)
                    val type = it.getInt(typeIdx)
                    val isRead = it.getInt(readIdx) == 1
                    val isFromMe = (type == Telephony.Sms.MESSAGE_TYPE_SENT || type == Telephony.Sms.MESSAGE_TYPE_OUTBOX)

                    val msg = com.example.core.model.Message(
                        id = id,
                        threadId = threadId,
                        address = address,
                        body = body,
                        date = date,
                        isFromMe = isFromMe,
                        isRead = isRead
                    )
                    messages.add(Pair(threadId, msg))
                }
            }
        } catch (_: Exception) {}
        messages
    }
}
