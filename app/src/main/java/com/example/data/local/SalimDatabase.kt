package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MessageFtsEntity::class,
        ContactEntity::class,
        BlockedNumberEntity::class,
        ScheduledMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao

    companion object {
        @Volatile
        private var INSTANCE: SalimDatabase? = null

        fun getInstance(context: Context): SalimDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SalimDatabase::class.java,
                    "salim_sms.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
