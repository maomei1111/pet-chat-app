package com.maomei.petchatapp.data.photo

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

/**
 * 仕様書 7.2/7.3 の OAuth 認可（`photospicker.mediaitems.readonly` スコープ）を、
 * Google Play services の Authorization API（`Identity.getAuthorizationClient`）経由で行う。
 *
 * Android用OAuthクライアントは Google Cloud Console 側でパッケージ名＋署名SHA-1に紐づけて
 * 登録するため、このクラス自体はクライアントIDを保持しない（Play servicesが端末の署名から解決する）。
 * 未同意・未許可の場合は [AuthorizationResult.hasResolution] が true になり、呼び出し元
 * （Compose側）が [AuthorizationResult.pendingIntent] を `ActivityResultContracts.StartIntentSenderForResult`
 * で起動し、その結果を [resultFromIntent] に渡してトークンを取り出す。
 */
class GoogleOAuthService(private val context: Context) {

    private val scope = Scope(PHOTOS_PICKER_SCOPE)
    private val client get() = Identity.getAuthorizationClient(context)

    /** 認可を要求する。既に許可済みならこの時点で `accessToken` が入って返る。 */
    suspend fun requestAuthorization(): AuthorizationResult {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(scope))
            .build()
        return client.authorize(request).await()
    }

    /** `StartIntentSenderForResult` の結果 Intent からトークンを含む結果を取り出す。失敗時は null。 */
    fun resultFromIntent(intent: Intent): AuthorizationResult? =
        runCatching { client.getAuthorizationResultFromIntent(intent) }.getOrNull()

    companion object {
        const val PHOTOS_PICKER_SCOPE = "https://www.googleapis.com/auth/photospicker.mediaitems.readonly"
    }
}
