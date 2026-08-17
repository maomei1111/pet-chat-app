package com.maomei.petchatapp.data.photo

/** 仕様書 7.3 の Picker セッション状態（Google Photos Picker API `sessions` レスポンス相当）。 */
data class GooglePickerSession(
    val id: String,
    val pickerUri: String,
    val pollIntervalSeconds: Long,
    val timeoutSeconds: Long,
    val mediaItemsSet: Boolean
)

/** Picker セッションで選択された1件のメディア（`mediaItems` レスポンス相当）。 */
data class GoogleMediaItem(
    val id: String,
    val baseUrl: String,
    val mimeType: String
) {
    val isPhoto: Boolean get() = mimeType.startsWith("image/")
}
