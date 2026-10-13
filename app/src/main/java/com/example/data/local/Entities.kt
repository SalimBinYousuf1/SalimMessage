package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
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

@Entity(
    tableName = "messages",
    indices = [
        Index("threadId"),
        Index("date"),
        Index("address")
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isFromMe: Boolean,
    val status: String,
    val type: String,
    val attachmentUri: String? = null,
    val subscriptionId: Int = -1,
    val isRead: Boolean = true,
    val isStarred: Boolean = false,
    val otpCode: String? = null,
    val isSpam: Boolean = false
)

@Entity(tableName = "messages_fts")
@Fts4(contentEntity = MessageEntity::class)
data class MessageFtsEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowid: Long,
    val body: String,
    val address: String
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val lookupKey: String,
    val name: String,
    val number: String,
    val photoUri: String? = null
)

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val reason: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipient: String,
    val body: String,
    val scheduledTime: Long,
    val subscriptionId: Int = -1,
    val isSent: Boolean = false
)
