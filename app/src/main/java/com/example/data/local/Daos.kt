package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("""
        SELECT * FROM conversations 
        WHERE isArchived = 0 AND isSpam = 0 
        ORDER BY isPinned DESC, lastMessageDate DESC
    """)
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageDate DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isSpam = 1 ORDER BY lastMessageDate DESC")
    fun getSpamConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isFavourite = 1 ORDER BY lastMessageDate DESC")
    fun getFavouriteConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE threadId = :threadId LIMIT 1")
    suspend fun getConversationByThreadId(threadId: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE address = :address LIMIT 1")
    suspend fun getConversationByAddress(address: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE threadId = :threadId")
    suspend fun setPinned(threadId: Long, isPinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE threadId = :threadId")
    suspend fun setArchived(threadId: Long, isArchived: Boolean)

    @Query("UPDATE conversations SET isMuted = :isMuted WHERE threadId = :threadId")
    suspend fun setMuted(threadId: Long, isMuted: Boolean)

    @Query("UPDATE conversations SET isFavourite = :isFav WHERE threadId = :threadId")
    suspend fun setFavourite(threadId: Long, isFav: Boolean)

    @Query("UPDATE conversations SET isSpam = :isSpam WHERE threadId = :threadId")
    suspend fun setSpam(threadId: Long, isSpam: Boolean)

    @Query("UPDATE conversations SET isBlocked = :isBlocked WHERE threadId = :threadId")
    suspend fun setBlocked(threadId: Long, isBlocked: Boolean)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markAsRead(threadId: Long)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE threadId = :threadId")
    suspend fun incrementUnread(threadId: Long)

    @Query("UPDATE conversations SET draftMessage = :draft WHERE threadId = :threadId")
    suspend fun setDraft(threadId: Long, draft: String?)

    @Query("DELETE FROM conversations WHERE threadId = :threadId")
    suspend fun deleteConversation(threadId: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY date ASC")
    fun getMessagesForThread(threadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMessageForThread(threadId: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE isStarred = 1 ORDER BY date DESC")
    fun getStarredMessages(): Flow<List<MessageEntity>>

    @Query("""
        SELECT messages.* FROM messages
        JOIN messages_fts ON messages.id = messages_fts.rowid
        WHERE messages_fts MATCH :query
        ORDER BY messages.date DESC
    """)
    suspend fun searchMessagesFts(query: String): List<MessageEntity>

    @Query("""
        SELECT * FROM messages 
        WHERE body LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    suspend fun searchMessagesLike(query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE messages SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Query("UPDATE messages SET isRead = 1 WHERE threadId = :threadId")
    suspend fun markThreadAsRead(threadId: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: Long)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessageCount(): Int
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR number LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchContacts(query: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE number = :number OR number = :normalizedNumber LIMIT 1")
    suspend fun findContactByNumber(number: String, normalizedNumber: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("DELETE FROM contacts")
    suspend fun clearContacts()
}

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY dateAdded DESC")
    fun getAllBlocked(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE number = :number)")
    suspend fun isBlocked(number: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blocked: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE number = :number")
    suspend fun deleteByNumber(number: String)
}

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages WHERE isSent = 0 ORDER BY scheduledTime ASC")
    fun getPendingMessages(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE isSent = 0 AND scheduledTime <= :now")
    suspend fun getDueMessages(now: Long): List<ScheduledMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ScheduledMessageEntity): Long

    @Query("UPDATE scheduled_messages SET isSent = 1 WHERE id = :id")
    suspend fun markSent(id: Long)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun delete(id: Long)
}
