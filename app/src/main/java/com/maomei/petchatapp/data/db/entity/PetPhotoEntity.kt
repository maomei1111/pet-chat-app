package com.maomei.petchatapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maomei.petchatapp.data.model.PhotoSource

@Entity(tableName = "pet_photo")
data class PetPhotoEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val localPath: String,
    val source: PhotoSource,
    val googleMediaItemId: String?,
    val isMain: Boolean,
    val createdAt: Long
)
