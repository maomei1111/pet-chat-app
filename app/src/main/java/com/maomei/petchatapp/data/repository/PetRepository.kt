package com.maomei.petchatapp.data.repository

import com.maomei.petchatapp.data.db.PetProfileDao
import com.maomei.petchatapp.data.db.toDomain
import com.maomei.petchatapp.data.db.toEntity
import com.maomei.petchatapp.data.model.PetProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetRepository(private val dao: PetProfileDao) {

    fun observeActiveProfile(): Flow<PetProfile?> =
        dao.observeActiveProfile().map { it?.toDomain() }

    suspend fun getActiveProfile(): PetProfile? = dao.getActiveProfile()?.toDomain()

    suspend fun save(profile: PetProfile) = dao.insert(profile.toEntity())

    suspend fun update(profile: PetProfile) = dao.update(profile.toEntity())

    suspend fun resetAll() = dao.deleteAll()
}
