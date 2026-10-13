package com.example.core.model

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED,
    RECEIVED
}

enum class MessageType {
    SMS,
    MMS,
    AUDIO,
    IMAGE,
    VIDEO,
    VCARD,
    FILE
}

enum class ConversationCategory {
    ALL,
    CONTACTS,
    UNKNOWN,
    NEW, // Unread
    ARCHIVED,
    SPAM,
    FAVOURITE
}

data class SimInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String? = null
)

data class Contact(
    val id: String,
    val lookupKey: String,
    val name: String,
    val number: String,
    val photoUri: String? = null
)

data class Message(
    val id: Long = 0,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isFromMe: Boolean,
    val status: MessageStatus = if (isFromMe) MessageStatus.SENT else MessageStatus.RECEIVED,
    val type: MessageType = MessageType.SMS,
    val attachmentUri: String? = null,
    val subscriptionId: Int = -1,
    val isRead: Boolean = true,
    val isStarred: Boolean = false,
    val otpCode: String? = null,
    val isSpam: Boolean = false
)

data class Conversation(
    val threadId: Long,
    val address: String,
    val contactName: String? = null,
    val contactPhotoUri: String? = null,
    val lastMessageBody: String = "",
    val lastMessageDate: Long = 0L,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val isFavourite: Boolean = false,
    val isSpam: Boolean = false,
    val isBlocked: Boolean = false,
    val draftMessage: String? = null,
    val subscriptionId: Int = -1
)

data class ScheduledMessage(
    val id: Long = 0,
    val recipient: String,
    val body: String,
    val scheduledTime: Long,
    val subscriptionId: Int = -1,
    val isSent: Boolean = false
)

data class BlockedNumber(
    val id: Long = 0,
    val number: String,
    val reason: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
