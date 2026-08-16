package com.maomei.petchatapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maomei.petchatapp.data.model.MessageCategory
import com.maomei.petchatapp.data.model.ReplySource
import com.maomei.petchatapp.data.model.Sender

@Entity(tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val sender: Sender,
    val text: String,
    val photoId: String?,
    val category: MessageCategory?,
    val replySource: ReplySource?,
    val createdAt: Long
)
