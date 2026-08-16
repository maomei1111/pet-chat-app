package com.maomei.petchatapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maomei.petchatapp.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE petId = :petId ORDER BY createdAt ASC")
    fun observeMessagesForPet(petId: String): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_message WHERE petId = :petId")
    suspend fun deleteAllForPet(petId: String)
}
