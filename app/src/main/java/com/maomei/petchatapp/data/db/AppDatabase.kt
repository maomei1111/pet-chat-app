package com.maomei.petchatapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.maomei.petchatapp.data.db.entity.ChatMessageEntity
import com.maomei.petchatapp.data.db.entity.PetPhotoEntity
import com.maomei.petchatapp.data.db.entity.PetProfileEntity

@Database(
    entities = [PetProfileEntity::class, PetPhotoEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petProfileDao(): PetProfileDao
    abstract fun petPhotoDao(): PetPhotoDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_chat_app.db"
                ).build().also { instance = it }
            }
    }
}
