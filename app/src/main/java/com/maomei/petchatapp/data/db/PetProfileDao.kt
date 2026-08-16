package com.maomei.petchatapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maomei.petchatapp.data.db.entity.PetProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: PetProfileEntity)

    @Update
    suspend fun update(profile: PetProfileEntity)

    @Delete
    suspend fun delete(profile: PetProfileEntity)

    @Query("DELETE FROM pet_profile")
    suspend fun deleteAll()

    /** 初期版はペット1匹のみ。作成日時が最も古いものを「現在のペット」として扱う。 */
    @Query("SELECT * FROM pet_profile ORDER BY createdAt ASC LIMIT 1")
    fun observeActiveProfile(): Flow<PetProfileEntity?>

    @Query("SELECT * FROM pet_profile ORDER BY createdAt ASC LIMIT 1")
    suspend fun getActiveProfile(): PetProfileEntity?

    @Query("SELECT * FROM pet_profile WHERE id = :id")
    suspend fun getById(id: String): PetProfileEntity?
}
