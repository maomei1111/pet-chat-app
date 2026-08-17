package com.maomei.petchatapp.data.photo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Google Photos Picker API（https://developers.google.com/photos/picker/guides/get-started-picker）
 * への直接のHTTP呼び出し（仕様書 7.3, 7.4）。
 */
class GooglePhotosApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createSession(accessToken: String): GooglePickerSession = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/sessions")
            .addHeader("Authorization", "Bearer $accessToken")
            .post("{}".toRequestBody(jsonMediaType))
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "createSession failed: ${response.code}" }
            parseSession(JSONObject(response.body!!.string()))
        }
    }

    suspend fun getSession(accessToken: String, sessionId: String): GooglePickerSession =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$BASE_URL/sessions/$sessionId")
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "getSession failed: ${response.code}" }
                parseSession(JSONObject(response.body!!.string()))
            }
        }

    suspend fun deleteSession(accessToken: String, sessionId: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/sessions/$sessionId")
            .addHeader("Authorization", "Bearer $accessToken")
            .delete()
            .build()
        runCatching { client.newCall(request).execute().close() }
        Unit
    }

    /** 選択済みメディア一覧を全ページ取得する。 */
    suspend fun listMediaItems(accessToken: String, sessionId: String): List<GoogleMediaItem> =
        withContext(Dispatchers.IO) {
            val items = mutableListOf<GoogleMediaItem>()
            var pageToken: String? = null
            do {
                val urlBuilder = StringBuilder("$BASE_URL/mediaItems?sessionId=$sessionId&pageSize=100")
                if (!pageToken.isNullOrEmpty()) urlBuilder.append("&pageToken=$pageToken")
                val request = Request.Builder()
                    .url(urlBuilder.toString())
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "listMediaItems failed: ${response.code}" }
                    val body = JSONObject(response.body!!.string())
                    val array = body.optJSONArray("mediaItems")
                    if (array != null) {
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val mediaFile = item.optJSONObject("mediaFile") ?: continue
                            items += GoogleMediaItem(
                                id = item.optString("id"),
                                baseUrl = mediaFile.optString("baseUrl"),
                                mimeType = mediaFile.optString("mimeType")
                            )
                        }
                    }
                    pageToken = body.optString("nextPageToken", "").ifEmpty { null }
                }
            } while (pageToken != null)
            items
        }

    /** `baseUrl` は一時URLのため、選択直後にダウンロードしてローカルへ保存する（仕様 7.6）。 */
    suspend fun downloadImageBytes(accessToken: String, baseUrl: String): ByteArray =
        withContext(Dispatchers.IO) {
            // "=d" はGoogle Photos系APIの画像ダウンロード用パラメータ規約。
            val request = Request.Builder()
                .url("$baseUrl=d")
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "downloadImageBytes failed: ${response.code}" }
                response.body!!.bytes()
            }
        }

    private fun parseSession(body: JSONObject): GooglePickerSession {
        val pollingConfig = body.optJSONObject("pollingConfig")
        return GooglePickerSession(
            id = body.getString("id"),
            pickerUri = body.getString("pickerUri"),
            pollIntervalSeconds = pollingConfig?.optString("pollInterval")?.let(::parseDurationSeconds) ?: 5L,
            timeoutSeconds = pollingConfig?.optString("timeoutIn")?.let(::parseDurationSeconds) ?: 300L,
            mediaItemsSet = body.optBoolean("mediaItemsSet", false)
        )
    }

    /** APIは "5s" のような protobuf Duration 文字列を返す。 */
    private fun parseDurationSeconds(raw: String): Long =
        raw.trimEnd('s').toDoubleOrNull()?.toLong() ?: 5L

    companion object {
        private const val BASE_URL = "https://photospicker.googleapis.com/v1"
    }
}
