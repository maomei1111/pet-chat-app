package com.maomei.petchatapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength

/**
 * 仕様書 8章のデータモデル案に対応する Room Entity。
 * 初期版はペット1匹のみ登録可能だが、将来の拡張を見越してテーブル自体は複数行を許容する。
 */
@Entity(tableName = "pet_profile")
data class PetProfileEntity(
    @PrimaryKey val id: String,
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
