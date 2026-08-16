package com.maomei.petchatapp.data.ai

import com.maomei.petchatapp.data.model.MessageCategory
import com.maomei.petchatapp.data.model.PetProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 自前のバックエンド（Railway等でホストするプロキシAPI）経由でAI返信を取得する実装。
 * OpenAIのAPIキーはバックエンド側にのみ置き、アプリ（APK）には一切含めない。
 *
 * [baseUrl] または [sharedSecret] が空、通信エラー・タイムアウト・不正レスポンスの場合は
 * 必ず null を返し、呼び出し元が 7.3 のフォールバック文言を表示する。
 */
class BackendAiReplyProvider(
    private val baseUrl: String,
    private val sharedSecret: String
) : AiReplyProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateReply(
        pet: PetProfile,
        userText: String,
        category: MessageCategory
    ): String? {
        if (baseUrl.isBlank() || sharedSecret.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject()
                    .put("species", pet.species.name)
                    .put("name", pet.name)
                    .put("personality", pet.personality.displayName)
                    .put("firstPerson", pet.firstPerson)
                    .put("ownerCallName", pet.ownerCallName)
                    .put("replyLength", pet.replyLength.name)
                    .put("useEmoji", pet.useEmoji)
                    .put("userText", userText)

                val request = Request.Builder()
                    .url("${baseUrl.trimEnd('/')}/v1/reply")
                    .addHeader("Authorization", "Bearer $sharedSecret")
                    .addHeader("Content-Type", "application/json")
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    JSONObject(body).optString("reply").takeIf { it.isNotBlank() }
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
