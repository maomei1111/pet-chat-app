package com.maomei.petchatapp.di

import android.content.Context
import com.maomei.petchatapp.BuildConfig
import com.maomei.petchatapp.data.ai.AiReplyProvider
import com.maomei.petchatapp.data.ai.BackendAiReplyProvider
import com.maomei.petchatapp.data.db.AppDatabase
import com.maomei.petchatapp.data.photo.PhotoPickerService
import com.maomei.petchatapp.data.photo.SystemPhotoPickerService
import com.maomei.petchatapp.data.repository.ChatRepository
import com.maomei.petchatapp.data.repository.PetRepository
import com.maomei.petchatapp.data.repository.PhotoRepository
import com.maomei.petchatapp.reply.ReplyEngine

/**
 * Hilt/Dagger を使わない、手動 DI コンテナ。
 * [com.maomei.petchatapp.PetChatApplication] が単一インスタンスを保持し、
 * ViewModel 生成時のファクトリからこれを参照する。
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    // 将来 Google Photos Picker API 実装に差し替える際はここを変更するだけでよい。
    val photoPickerService: PhotoPickerService by lazy { SystemPhotoPickerService(appContext) }

    // バックエンド（backend/ をRailway等にデプロイしたもの）のURLと共有シークレットは
    // local.properties の backend.base.url / backend.shared.secret から供給される（app/build.gradle.kts 参照）。
    // OpenAIのAPIキーはバックエンド側にのみ置かれ、アプリには一切含まれない。
    // 未設定時は常に null を返す（＝フォールバック応答になる）ため、未設定でも安全に動作する。
    val aiReplyProvider: AiReplyProvider by lazy {
        BackendAiReplyProvider(
            baseUrl = BuildConfig.BACKEND_BASE_URL,
            sharedSecret = BuildConfig.BACKEND_SHARED_SECRET
        )
    }

    val petRepository: PetRepository by lazy { PetRepository(database.petProfileDao()) }
    val photoRepository: PhotoRepository by lazy { PhotoRepository(database.petPhotoDao(), photoPickerService) }
    val chatRepository: ChatRepository by lazy { ChatRepository(database.chatMessageDao()) }

    val replyEngine: ReplyEngine by lazy { ReplyEngine(aiReplyProvider) }
}
