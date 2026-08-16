package com.maomei.petchatapp.data.db

import com.maomei.petchatapp.data.db.entity.ChatMessageEntity
import com.maomei.petchatapp.data.db.entity.PetPhotoEntity
import com.maomei.petchatapp.data.db.entity.PetProfileEntity
import com.maomei.petchatapp.data.model.ChatMessage
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.PetProfile

fun PetProfileEntity.toDomain() = PetProfile(
    id = id,
    name = name,
    species = species,
    personality = personality,
    ownerCallName = ownerCallName,
    firstPerson = firstPerson,
    replyLength = replyLength,
    useEmoji = useEmoji,
    mainPhotoId = mainPhotoId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PetProfile.toEntity() = PetProfileEntity(
    id = id,
    name = name,
    species = species,
    personality = personality,
    ownerCallName = ownerCallName,
    firstPerson = firstPerson,
    replyLength = replyLength,
    useEmoji = useEmoji,
    mainPhotoId = mainPhotoId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PetPhotoEntity.toDomain() = PetPhoto(
    id = id,
    petId = petId,
    localPath = localPath,
    source = source,
    googleMediaItemId = googleMediaItemId,
    isMain = isMain,
    createdAt = createdAt
)

fun PetPhoto.toEntity() = PetPhotoEntity(
    id = id,
    petId = petId,
    localPath = localPath,
    source = source,
    googleMediaItemId = googleMediaItemId,
    isMain = isMain,
    createdAt = createdAt
)

fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    petId = petId,
    sender = sender,
    text = text,
    photoId = photoId,
    category = category,
    replySource = replySource,
    createdAt = createdAt
)

fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    petId = petId,
    sender = sender,
    text = text,
    photoId = photoId,
    category = category,
    replySource = replySource,
    createdAt = createdAt
)
