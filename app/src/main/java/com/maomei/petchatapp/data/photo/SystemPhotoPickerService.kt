package com.maomei.petchatapp.data.photo

import android.content.Context
import android.net.Uri
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.PhotoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Android 標準 System Photo Picker (`ActivityResultContracts.PickMultipleVisualMedia`) で
 * 選択された画像 URI を `context.filesDir/pet_photos/` へコピーして永続化する暫定実装。
 *
 * 仕様書 4.4 の「Pickerで選択された写真以外を自動取得してはならない」を満たすため、
 * 呼び出し元から渡された URI 一覧のみを処理し、それ以外の画像には一切アクセスしない。
 */
class SystemPhotoPickerService(private val context: Context) : PhotoPickerService {

    private val photoDir: File
        get() = File(context.filesDir, PHOTO_DIR_NAME).apply { if (!exists()) mkdirs() }

    override suspend fun importPhotos(petId: String, uris: List<Uri>): List<PetPhoto> =
        withContext(Dispatchers.IO) {
            uris.mapNotNull { uri -> copyToLocalStorage(petId, uri) }
        }

    override suspend fun deletePhotoFile(localPath: String) = withContext(Dispatchers.IO) {
        runCatching { File(localPath).takeIf { it.exists() }?.delete() }
        Unit
    }

    private fun copyToLocalStorage(petId: String, uri: Uri): PetPhoto? {
        return runCatching {
            val fileName = "${UUID.randomUUID()}.jpg"
            val destFile = File(photoDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            PetPhoto(
                id = UUID.randomUUID().toString(),
                petId = petId,
                localPath = destFile.absolutePath,
                source = PhotoSource.SYSTEM_PICKER,
                googleMediaItemId = null,
                isMain = false,
                createdAt = System.currentTimeMillis()
            )
        }.getOrNull()
    }

    companion object {
        const val PHOTO_DIR_NAME = "pet_photos"
        const val MIN_RECOMMENDED_PHOTOS = 3
        const val MAX_PHOTOS = 50
    }
}
