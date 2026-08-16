package com.maomei.petchatapp.data.ai

import com.maomei.petchatapp.data.model.MessageCategory
import com.maomei.petchatapp.data.model.PetProfile

/**
 * AI 返信生成の抽象。仕様書 5.2 手順5・7章に対応。
 * テンプレートで判定できないカテゴリ（判定不能）の入力のみ、この経路を通る想定。
 *
 * このタスクでは実際のネットワーク呼び出しは実装しない（[StubAiReplyProvider] 参照）。
 * 将来、本物の AI プロバイダ（LLM API 等）に差し替える際はこのインターフェースの
 * 実装クラスを追加し、[com.maomei.petchatapp.di.AppContainer] での紐付けを変更するだけでよい。
 */
interface AiReplyProvider {
    /**
     * @return 生成できた返信文字列。生成に失敗した場合（通信エラー・タイムアウト等）は null を返し、
     * 呼び出し元は 7.3 のフォールバック文言を表示する。
     */
    suspend fun generateReply(pet: PetProfile, userText: String, category: MessageCategory): String?
}

/**
 * 常に null を返すスタブ実装。実ネットワーク呼び出しは一切行わない。
 * AI失敗として扱われ、呼び出し元が [com.maomei.petchatapp.reply.FallbackReplies] から
 * フォールバック文言を選ぶ。
 */
class StubAiReplyProvider : AiReplyProvider {
    override suspend fun generateReply(
        pet: PetProfile,
        userText: String,
        category: MessageCategory
    ): String? = null
}
