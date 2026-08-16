package com.maomei.petchatapp.data.db

import androidx.room.TypeConverter
import com.maomei.petchatapp.data.model.MessageCategory
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.PhotoSource
import com.maomei.petchatapp.data.model.ReplyLength
import com.maomei.petchatapp.data.model.ReplySource
import com.maomei.petchatapp.data.model.Sender

/** すべての enum を文字列として保存するための Room TypeConverter 群。 */
class Converters {
    @TypeConverter
    fun fromPetSpecies(value: PetSpecies): String = value.name

    @TypeConverter
    fun toPetSpecies(value: String): PetSpecies = PetSpecies.valueOf(value)

    @TypeConverter
    fun fromPersonality(value: Personality): String = value.name

    @TypeConverter
    fun toPersonality(value: String): Personality = Personality.valueOf(value)

    @TypeConverter
    fun fromReplyLength(value: ReplyLength): String = value.name

    @TypeConverter
    fun toReplyLength(value: String): ReplyLength = ReplyLength.valueOf(value)

    @TypeConverter
    fun fromPhotoSource(value: PhotoSource): String = value.name

    @TypeConverter
    fun toPhotoSource(value: String): PhotoSource = PhotoSource.valueOf(value)

    @TypeConverter
    fun fromSender(value: Sender): String = value.name

    @TypeConverter
    fun toSender(value: String): Sender = Sender.valueOf(value)

    @TypeConverter
    fun fromMessageCategory(value: MessageCategory?): String? = value?.name

    @TypeConverter
    fun toMessageCategory(value: String?): MessageCategory? = value?.let { MessageCategory.valueOf(it) }

    @TypeConverter
    fun fromReplySource(value: ReplySource?): String? = value?.name

    @TypeConverter
    fun toReplySource(value: String?): ReplySource? = value?.let { ReplySource.valueOf(it) }
}
