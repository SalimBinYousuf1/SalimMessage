package com.example.data.repository

import android.content.Context
import com.example.core.model.BlockedNumber
import com.example.core.model.Contact
import com.example.core.model.Conversation
import com.example.core.model.Message
import com.example.core.model.MessageStatus
import com.example.core.model.MessageType
import com.example.core.model.ScheduledMessage
import com.example.core.model.SimInfo
import com.example.core.preferences.PreferencesManager
import com.example.core.telephony.TelephonyManagerHelper
import com.example.core.util.OtpDetector
import com.example.core.util.PhoneNumberHelper
import com.example.core.util.SpamDetector
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.ContactEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.SalimDatabase
import com.example.data.local.ScheduledMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmsRepository(
    private val context: Context,
    private val database: SalimDatabase,
    private val telephonyHelper: TelephonyManagerHelper,
    private val preferencesManager: PreferencesManager
) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val contactDao = database.contactDao()
    private val blockedNumberDao = database.blockedNumberDao()
    private val scheduledMessageDao = database.scheduledMessageDao()

    val conversationsFlow: Flow<List<Conversation>> = conversationDao.getActiveConversations().map { list ->
        list.map { it.toDomain() }
    }

    val archivedConversationsFlow: Flow<List<Conversation>> = conversationDao.getArchivedConversations().map { list ->
        list.map { it.toDomain() }
    }

    val spamConversationsFlow: Flow<List<Conversation>> = conversationDao.getSpamConversations().map { list ->
        list.map { it.toDomain() }
    }

    val favouriteConversationsFlow: Flow<List<Conversation>> = conversationDao.getFavouriteConversations().map { list ->
        list.map { it.toDomain() }
    }

    val blockedNumbersFlow: Flow<List<BlockedNumber>> = blockedNumberDao.getAllBlocked().map { list ->
        list.map { BlockedNumber(it.id, it.number, it.reason, it.dateAdded) }
    }

    val scheduledMessagesFlow: Flow<List<ScheduledMessage>> = scheduledMessageDao.getPendingMessages().map { list ->
        list.map { ScheduledMessage(it.id, it.recipient, it.body, it.scheduledTime, it.subscriptionId, it.isSent) }
    }

    val contactsFlow: Flow<List<Contact>> = contactDao.getAllContacts().map { list ->
        list.map { Contact(it.id, it.lookupKey, it.name, it.number, it.photoUri) }
    }

    fun getMessagesForThread(threadId: Long): Flow<List<Message>> {
        return messageDao.getMessagesForThread(threadId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getConversation(threadId: Long): Conversation? {
        return conversationDao.getConversationByThreadId(threadId)?.toDomain()
    }

    fun isDefaultSmsApp(): Boolean = telephonyHelper.isDefaultSmsApp()

    fun getActiveSimCards(): List<SimInfo> = telephonyHelper.getActiveSimCards()

    suspend fun syncContacts() = withContext(Dispatchers.IO) {
        val contacts = telephonyHelper.queryDeviceContacts()
        if (contacts.isNotEmpty()) {
            contactDao.insertContacts(contacts.map {
                ContactEntity(it.id, it.lookupKey, it.name, it.number, it.photoUri)
            })
        }
    }

    suspend fun syncInitialMessages() = withContext(Dispatchers.IO) {
        syncContacts()
        val imported = telephonyHelper.importExistingSms()
        if (imported.isEmpty()) return@withContext

        val messageEntities = mutableListOf<MessageEntity>()
        val convMap = mutableMapOf<Long, ConversationEntity>()

        for ((threadId, msg) in imported) {
            val contact = contactDao.findContactByNumber(msg.address, PhoneNumberHelper.normalize(msg.address))
            val isSpam = SpamDetector.isSuspectedSpam(msg.address, msg.body)
            val otp = OtpDetector.extractOtp(msg.body)

            messageEntities.add(
                MessageEntity(
                    id = msg.id,
                    threadId = threadId,
                    address = msg.address,
                    body = msg.body,
                    date = msg.date,
                    isFromMe = msg.isFromMe,
                    status = msg.status.name,
                    type = msg.type.name,
                    attachmentUri = msg.attachmentUri,
                    subscriptionId = msg.subscriptionId,
                    isRead = msg.isRead,
                    otpCode = otp,
                    isSpam = isSpam
                )
            )

            val existingConv = convMap[threadId]
            if (existingConv == null || msg.date > existingConv.lastMessageDate) {
                convMap[threadId] = ConversationEntity(
                    threadId = threadId,
                    address = msg.address,
                    contactName = contact?.name,
                    contactPhotoUri = contact?.photoUri,
                    lastMessageBody = msg.body,
                    lastMessageDate = msg.date,
                    unreadCount = if (!msg.isRead) 1 else 0,
                    isSpam = isSpam
                )
            }
        }

        messageDao.insertMessages(messageEntities)
        conversationDao.insertOrUpdateAll(convMap.values.toList())
    }

    suspend fun sendMessage(
        recipient: String,
        body: String,
        subId: Int = -1,
        attachmentUri: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val normalized = PhoneNumberHelper.normalize(recipient)
        var conv = conversationDao.getConversationByAddress(recipient)
        val threadId = conv?.threadId ?: System.currentTimeMillis()

        val contact = contactDao.findContactByNumber(recipient, normalized)
        val msgType = if (attachmentUri != null) MessageType.IMAGE else MessageType.SMS

        val messageEntity = MessageEntity(
            threadId = threadId,
            address = recipient,
            body = body,
            date = System.currentTimeMillis(),
            isFromMe = true,
            status = MessageStatus.SENDING.name,
            type = msgType.name,
            attachmentUri = attachmentUri,
            subscriptionId = subId,
            isRead = true
        )
        val msgId = messageDao.insertMessage(messageEntity)

        val updatedConv = ConversationEntity(
            threadId = threadId,
            address = recipient,
            contactName = contact?.name ?: conv?.contactName,
            contactPhotoUri = contact?.photoUri ?: conv?.contactPhotoUri,
            lastMessageBody = body,
            lastMessageDate = System.currentTimeMillis(),
            unreadCount = 0,
            isPinned = conv?.isPinned ?: false,
            isArchived = conv?.isArchived ?: false,
            isMuted = conv?.isMuted ?: false,
            isFavourite = conv?.isFavourite ?: false,
            draftMessage = null,
            subscriptionId = subId
        )
        conversationDao.insertOrUpdate(updatedConv)

        try {
            telephonyHelper.sendTextMessage(recipient, body, subId, msgId)
            messageDao.updateStatus(msgId, MessageStatus.SENT.name)
        } catch (_: Exception) {
            messageDao.updateStatus(msgId, MessageStatus.FAILED.name)
        }

        msgId
    }

    suspend fun receiveIncomingMessage(
        sender: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        subId: Int = -1
    ): Long = withContext(Dispatchers.IO) {
        val isBlocked = blockedNumberDao.isBlocked(sender) || blockedNumberDao.isBlocked(PhoneNumberHelper.normalize(sender))
        if (isBlocked) return@withContext -1L

        val isSpam = SpamDetector.isSuspectedSpam(sender, body)
        val otp = OtpDetector.extractOtp(body)
        val contact = contactDao.findContactByNumber(sender, PhoneNumberHelper.normalize(sender))

        var conv = conversationDao.getConversationByAddress(sender)
        val threadId = conv?.threadId ?: System.currentTimeMillis()

        val messageEntity = MessageEntity(
            threadId = threadId,
            address = sender,
            body = body,
            date = timestamp,
            isFromMe = false,
            status = MessageStatus.RECEIVED.name,
            type = MessageType.SMS.name,
            subscriptionId = subId,
            isRead = false,
            otpCode = otp,
            isSpam = isSpam
        )
        val msgId = messageDao.insertMessage(messageEntity)

        val updatedConv = ConversationEntity(
            threadId = threadId,
            address = sender,
            contactName = contact?.name ?: conv?.contactName,
            contactPhotoUri = contact?.photoUri ?: conv?.contactPhotoUri,
            lastMessageBody = body,
            lastMessageDate = timestamp,
            unreadCount = (conv?.unreadCount ?: 0) + 1,
            isPinned = conv?.isPinned ?: false,
            isArchived = conv?.isArchived ?: false,
            isMuted = conv?.isMuted ?: false,
            isFavourite = conv?.isFavourite ?: false,
            isSpam = isSpam,
            subscriptionId = subId
        )
        conversationDao.insertOrUpdate(updatedConv)

        // Write to system provider if default
        telephonyHelper.writeIncomingSmsToProvider(sender, body, timestamp, subId)

        msgId
    }

    suspend fun searchMessages(query: String): List<Message> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val ftsResults = messageDao.searchMessagesFts(query)
            if (ftsResults.isNotEmpty()) return@withContext ftsResults.map { it.toDomain() }
        } catch (_: Exception) {}
        messageDao.searchMessagesLike(query).map { it.toDomain() }
    }

    suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        contactDao.searchContacts(query).map { Contact(it.id, it.lookupKey, it.name, it.number, it.photoUri) }
    }

    suspend fun markThreadAsRead(threadId: Long) = withContext(Dispatchers.IO) {
        conversationDao.markAsRead(threadId)
        messageDao.markThreadAsRead(threadId)
    }

    suspend fun pinConversation(threadId: Long, pin: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(threadId, pin)
    }

    suspend fun archiveConversation(threadId: Long, archive: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setArchived(threadId, archive)
    }

    suspend fun muteConversation(threadId: Long, mute: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setMuted(threadId, mute)
    }

    suspend fun favouriteConversation(threadId: Long, fav: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setFavourite(threadId, fav)
    }

    suspend fun deleteConversation(threadId: Long) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversation(threadId)
        messageDao.deleteMessagesForThread(threadId)
    }

    suspend fun blockNumber(number: String, reason: String? = null) = withContext(Dispatchers.IO) {
        blockedNumberDao.insert(BlockedNumberEntity(number = number, reason = reason))
        val conv = conversationDao.getConversationByAddress(number)
        if (conv != null) {
            conversationDao.setBlocked(conv.threadId, true)
        }
    }

    suspend fun unblockNumber(number: String) = withContext(Dispatchers.IO) {
        blockedNumberDao.deleteByNumber(number)
        val conv = conversationDao.getConversationByAddress(number)
        if (conv != null) {
            conversationDao.setBlocked(conv.threadId, false)
        }
    }

    suspend fun reportNotSpam(threadId: Long) = withContext(Dispatchers.IO) {
        conversationDao.setSpam(threadId, false)
    }

    suspend fun setDraft(threadId: Long, draft: String?) = withContext(Dispatchers.IO) {
        conversationDao.setDraft(threadId, draft)
    }

    suspend fun scheduleMessage(recipient: String, body: String, scheduledTime: Long, subId: Int): Long = withContext(Dispatchers.IO) {
        scheduledMessageDao.insert(
            ScheduledMessageEntity(
                recipient = recipient,
                body = body,
                scheduledTime = scheduledTime,
                subscriptionId = subId
            )
        )
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        messageDao.deleteMessage(id)
    }

    suspend fun starMessage(id: Long, star: Boolean) = withContext(Dispatchers.IO) {
        messageDao.setStarred(id, star)
    }

    private fun ConversationEntity.toDomain() = Conversation(
        threadId = threadId,
        address = address,
        contactName = contactName,
        contactPhotoUri = contactPhotoUri,
        lastMessageBody = lastMessageBody,
        lastMessageDate = lastMessageDate,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isArchived = isArchived,
        isMuted = isMuted,
        isLocked = isLocked,
        isFavourite = isFavourite,
        isSpam = isSpam,
        isBlocked = isBlocked,
        draftMessage = draftMessage,
        subscriptionId = subscriptionId
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        threadId = threadId,
        address = address,
        body = body,
        date = date,
        isFromMe = isFromMe,
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
        type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.SMS),
        attachmentUri = attachmentUri,
        subscriptionId = subscriptionId,
        isRead = isRead,
        isStarred = isStarred,
        otpCode = otpCode,
        isSpam = isSpam
    )

    companion object {
        @Volatile
        private var INSTANCE: SmsRepository? = null

        fun getInstance(context: Context): SmsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val db = SalimDatabase.getInstance(context)
                    val telephony = TelephonyManagerHelper(context)
                    val prefs = PreferencesManager(context)
                    SmsRepository(context.applicationContext, db, telephony, prefs).also {
                        INSTANCE = it
                    }
                }
            }
        }
    }
}
