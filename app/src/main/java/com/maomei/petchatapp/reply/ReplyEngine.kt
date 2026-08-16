package com.maomei.petchatapp.reply

import com.maomei.petchatapp.data.ai.AiReplyProvider
import com.maomei.petchatapp.data.model.MessageCategory
import com.maomei.petchatapp.data.model.PetProfile
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.ReplySource

/** 仕様書 7.3 のAI失敗時フォールバック文言。 */
object FallbackReplies {
    private val messages = listOf(
        "うまく言えないけど、そばにいるよ",
        "それはむずかしいなあ",
        "今日はゆっくりしよ？"
    )

    fun random(): String = messages.random()
}

data class GeneratedReply(val text: String, val category: MessageCategory, val source: ReplySource)

/**
 * 仕様書 5.2 の送信処理（カテゴリ分類 → テンプレート優先 → だめならAI → だめならフォールバック）
 * と 6.1 の返信制約（1〜2文・原則40文字以内）を担うエンジン。
 *
 * 6.2 の「判定不能」は定義上どの定型カテゴリにも当てはまらない入力のため、
 * テンプレートは使わず必ずAI経路（[AiReplyProvider]）へ送る。AIキー未設定時や
 * 通信失敗時は 7.3 のフォールバック文言を返す。
 */
class ReplyEngine(private val aiReplyProvider: AiReplyProvider) {

    private val maxLength = 40

    suspend fun generateReply(pet: PetProfile, userText: String): GeneratedReply {
        val category = MessageClassifier.classify(userText)
        val templateCandidates = if (category == MessageCategory.UNKNOWN) {
            emptyList()
        } else {
            ReplyTemplates.candidates(
                species = pet.species,
                category = category,
                personality = pet.personality,
                replyLength = pet.replyLength
            )
        }

        if (templateCandidates.isNotEmpty()) {
            val chosen = templateCandidates.random()
            return GeneratedReply(
                text = finalize(chosen, pet),
                category = category,
                source = ReplySource.TEMPLATE
            )
        }

        // 判定不能などテンプレートが無い場合のみ AI 経路を試す。
        val aiText = aiReplyProvider.generateReply(pet, userText, category)
        if (aiText != null) {
            return GeneratedReply(text = finalize(aiText, pet), category = category, source = ReplySource.AI)
        }

        return GeneratedReply(
            text = enforceLength(FallbackReplies.random()),
            category = category,
            source = ReplySource.FALLBACK
        )
    }

    private fun finalize(rawTemplate: String, pet: PetProfile): String {
        var text = rawTemplate
            .replace("{firstPerson}", pet.firstPerson)
            .replace("{ownerCallName}", pet.ownerCallName)
        if (pet.useEmoji) {
            text += emojiFor(pet.species)
        }
        return enforceLength(text)
    }

    private fun emojiFor(species: PetSpecies): String = when (species) {
        PetSpecies.DOG -> "🐶"
        PetSpecies.CAT -> "🐱"
    }

    /** 6.1 の「原則40文字以内」を防御的に強制する。 */
    private fun enforceLength(text: String): String =
        if (text.length > maxLength) text.take(maxLength) else text
}
