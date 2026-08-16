package com.maomei.petchatapp.data.model

/**
 * 仕様書 8章のデータモデル案に対応するドメインモデル。
 * Room Entity ([com.maomei.petchatapp.data.db.entity]) とは Mapper で相互変換する。
 */
data class PetProfile(
    val id: String,
    val name: String,
    val species: PetSpecies,
    val personality: Personality,
    val ownerCallName: String,
    val firstPerson: String,
    val replyLength: ReplyLength,
    val useEmoji: Boolean,
    val mainPhotoId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class PetPhoto(
    val id: String,
    val petId: String,
    val localPath: String,
    val source: PhotoSource,
    val googleMediaItemId: String?,
    val isMain: Boolean,
    val createdAt: Long
)

data class ChatMessage(
    val id: String,
    val petId: String,
    val sender: Sender,
    val text: String,
    val photoId: String?,
    val category: MessageCategory?,
    val replySource: ReplySource?,
    val createdAt: Long
)
