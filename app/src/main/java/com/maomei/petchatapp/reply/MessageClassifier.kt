package com.maomei.petchatapp.reply

import com.maomei.petchatapp.data.model.MessageCategory

/**
 * 仕様書 6.2 のキーワードベース簡易分類ルール。
 *
 * 判定優先順位（重要）:
 * 1. SICK（体調不良）を TIRED（疲れた）より先に判定する（仕様 6.2 「体調不良を疲れたより優先判定」）。
 *    そのため「しんどい」は SICK 側で先に拾われる。
 * 2. EXPERT（専門的な質問）は医療・法律・金融などの明確なキーワードのみで判定する。
 * 3. どのカテゴリにも一致しなければ UNKNOWN（判定不能）。
 */
object MessageClassifier {

    private val sickKeywords = listOf("体調", "熱", "痛い", "しんどい")
    private val tiredKeywords = listOf("疲れた", "つかれた", "だるい")
    private val lonelyKeywords = listOf("寂しい", "さみしい", "不安")
    private val mealKeywords = listOf("ご飯", "ごはん", "お腹", "食べ", "おやつ")
    private val playKeywords = listOf("遊", "あそぼ")
    private val outingKeywords = listOf("ただいま", "おかえり", "行ってきます", "いってきます", "いってらっしゃい")
    private val greetingKeywords = listOf("おはよう", "こんにちは", "こんばんは", "やあ", "はじめまして")
    private val sleepKeywords = listOf("眠", "ねむ", "寝る", "おやすみ")
    private val workKeywords = listOf("仕事", "会社", "バイト", "残業")
    private val praiseKeywords = listOf("かわいい", "可愛い", "好き", "えらい", "偉い", "すごいね", "いい子")
    private val expertKeywords = listOf(
        "医療", "法律", "金融", "投資", "税金", "裁判", "病気", "薬",
        "弁護士", "診断", "治療", "株価", "契約", "確定申告", "資産運用"
    )

    fun classify(text: String): MessageCategory {
        val normalized = text.trim()
        if (normalized.isEmpty()) return MessageCategory.UNKNOWN

        return when {
            sickKeywords.any { normalized.contains(it) } -> MessageCategory.SICK
            tiredKeywords.any { normalized.contains(it) } -> MessageCategory.TIRED
            lonelyKeywords.any { normalized.contains(it) } -> MessageCategory.LONELY
            mealKeywords.any { normalized.contains(it) } -> MessageCategory.MEAL
            playKeywords.any { normalized.contains(it) } -> MessageCategory.PLAY
            outingKeywords.any { normalized.contains(it) } -> MessageCategory.OUTING
            greetingKeywords.any { normalized.contains(it) } -> MessageCategory.GREETING
            sleepKeywords.any { normalized.contains(it) } -> MessageCategory.SLEEP
            workKeywords.any { normalized.contains(it) } -> MessageCategory.WORK
            praiseKeywords.any { normalized.contains(it) } -> MessageCategory.PRAISE
            expertKeywords.any { normalized.contains(it) } -> MessageCategory.EXPERT
            else -> MessageCategory.UNKNOWN
        }
    }
}
