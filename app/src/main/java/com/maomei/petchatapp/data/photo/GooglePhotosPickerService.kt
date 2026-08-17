package com.maomei.petchatapp.data.photo

import android.content.Context
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.PhotoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Google Photos Picker API の正式連携（仕様書 7.3〜7.6）。
 *
 * [PhotoPickerService] とは異なり、選択が Google フォト側の別画面（ブラウザ/アプリ）で行われ、
 * セッションIDをポーリングして完了を検知するという非同期フローのため、同じインターフェースは
 * 実装せず専用のメソッド群を公開する。呼び出し順序は [createSession] → (pickerUriを起動) →
 * [pollUntilReady] → [importSelectedPhotos] → [deleteSession]。
 */
class GooglePhotosPickerService(
    private val context: Context,
    private val apiClient: GooglePhotosApiClient
) {
    private val photoDir: File
        get() = File(context.filesDir, SystemPhotoPickerService.PHOTO_DIR_NAME).apply { if (!exists()) mkdirs() }

    suspend fun createSession(accessToken: String): GooglePickerSession =
        apiClient.createSession(accessToken)

    /**
     * `mediaItemsSet` が true になるか、タイムアウトするまでAPI推奨間隔でポーリングする（仕様 7.5）。
     * @return 選択完了なら true、タイムアウトなら false
     */
    suspend fun pollUntilReady(accessToken: String, session: GooglePickerSession): Boolean {
        val deadline = System.currentTimeMillis() + session.timeoutSeconds * 1000
        var current = session
        while (System.currentTimeMillis() < deadline) {
            if (current.mediaItemsSet) return true
            delay(current.pollIntervalSeconds * 1000)
            current = apiClient.getSession(accessToken, session.id)
        }
        return current.mediaItemsSet
    }

    /** 選択済みメディアのうち画像のみをダウンロードし、端末内へ保存する（仕様 7.6）。 */
    suspend fun importSelectedPhotos(
        accessToken: String,
        petId: String,
        sessionId: String
    ): List<PetPhoto> = withContext(Dispatchers.IO) {
        apiClient.listMediaItems(accessToken, sessionId)
            .filter { it.isPhoto }
            .mapNotNull { item -> downloadAndSave(accessToken, petId, item) }
    }

    suspend fun deleteSession(accessToken: String, sessionId: String) {
        apiClient.deleteSession(accessToken, sessionId)
    }

    private suspend fun downloadAndSave(accessToken: String, petId: String, item: GoogleMediaItem): PetPhoto? =
        runCatching {
            val bytes = apiClient.downloadImageBytes(accessToken, item.baseUrl)
            val destFile = File(photoDir, "${UUID.randomUUID()}.jpg")
            destFile.writeBytes(bytes)
            PetPhoto(
                id = UUID.randomUUID().toString(),
                petId = petId,
                localPath = destFile.absolutePath,
                source = PhotoSource.GOOGLE_PHOTOS,
                googleMediaItemId = item.id,
                isMain = false,
                createdAt = System.currentTimeMillis()
            )
        }.getOrNull()
}
