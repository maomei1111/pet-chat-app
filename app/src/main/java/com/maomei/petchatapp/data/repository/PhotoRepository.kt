package com.maomei.petchatapp.data.repository

import android.net.Uri
import com.maomei.petchatapp.data.db.PetPhotoDao
import com.maomei.petchatapp.data.db.toDomain
import com.maomei.petchatapp.data.db.toEntity
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.photo.PhotoPickerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepository(
    private val dao: PetPhotoDao,
    private val photoPickerService: PhotoPickerService
) {
    fun observePhotosForPet(petId: String): Flow<List<PetPhoto>> =
        dao.observePhotosForPet(petId).map { list -> list.map { it.toDomain() } }

    suspend fun getPhotosForPet(petId: String): List<PetPhoto> =
        dao.getPhotosForPet(petId).map { it.toDomain() }

    /** System Photo Picker で選択された URI をアプリ専用領域へ取り込み、DB に登録する。 */
    suspend fun importPhotos(petId: String, uris: List<Uri>): List<PetPhoto> {
        val imported = photoPickerService.importPhotos(petId, uris)
        dao.insertAll(imported.map { it.toEntity() })
        return imported
    }

    suspend fun deletePhoto(photo: PetPhoto) {
        dao.delete(photo.toEntity())
        photoPickerService.deletePhotoFile(photo.localPath)
    }

    suspend fun setMainPhoto(petId: String, photoId: String) {
        dao.clearMainFlag(petId)
        dao.markMain(photoId)
    }

    suspend fun deleteAllForPet(petId: String) {
        getPhotosForPet(petId).forEach { photoPickerService.deletePhotoFile(it.localPath) }
        dao.deleteAllForPet(petId)
    }
}
