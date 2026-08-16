package com.maomei.petchatapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maomei.petchatapp.data.db.entity.PetPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PetPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PetPhotoEntity>)

    @Update
    suspend fun update(photo: PetPhotoEntity)

    @Delete
    suspend fun delete(photo: PetPhotoEntity)

    @Query("DELETE FROM pet_photo WHERE petId = :petId")
    suspend fun deleteAllForPet(petId: String)

    @Query("SELECT * FROM pet_photo WHERE petId = :petId ORDER BY createdAt ASC")
    fun observePhotosForPet(petId: String): Flow<List<PetPhotoEntity>>

    @Query("SELECT * FROM pet_photo WHERE petId = :petId ORDER BY createdAt ASC")
    suspend fun getPhotosForPet(petId: String): List<PetPhotoEntity>

    @Query("UPDATE pet_photo SET isMain = 0 WHERE petId = :petId")
    suspend fun clearMainFlag(petId: String)

    @Query("UPDATE pet_photo SET isMain = 1 WHERE id = :photoId")
    suspend fun markMain(photoId: String)
}
