package com.maomei.petchatapp.data.repository

import com.maomei.petchatapp.data.db.ChatMessageDao
import com.maomei.petchatapp.data.db.toDomain
import com.maomei.petchatapp.data.db.toEntity
import com.maomei.petchatapp.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val dao: ChatMessageDao) {

    fun observeMessagesForPet(petId: String): Flow<List<ChatMessage>> =
        dao.observeMessagesForPet(petId).map { list -> list.map { it.toDomain() } }

    suspend fun addMessage(message: ChatMessage) = dao.insert(message.toEntity())

    suspend fun clearHistory(petId: String) = dao.deleteAllForPet(petId)
}
