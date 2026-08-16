package com.maomei.petchatapp.reply

import com.maomei.petchatapp.data.model.MessageCategory
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength

/**
 * 仕様書 6.3 の返信例をベースにした、犬・猫 × カテゴリのテンプレート集。
 * `{firstPerson}` / `{ownerCallName}` はそれぞれペットの一人称・飼い主の呼び方に置換される。
 *
 * 各テンプレートは [短い] / [短い以外も許可] の目安として [isShort] を持つ。
 * 返信の長さ設定が「短い」の場合は isShort=true のもののみ、「普通」の場合は全候補から選ぶ。
 * すべてのテンプレートは 40 文字以内（プレースホルダ展開後の想定でも収まる長さ）で作成している。
 */
object ReplyTemplates {

    data class Template(val text: String, val isShort: Boolean = true)

    private val dogTemplates: Map<MessageCategory, List<Template>> = mapOf(
        MessageCategory.GREETING to listOf(
            Template("おはよう！{ownerCallName}"),
            Template("やあ、{ownerCallName}！げんき？", isShort = false),
            Template("こんにちは！{firstPerson}げんきだよ", isShort = false)
        ),
        MessageCategory.TIRED to listOf(
            Template("おつかれさま。そばにいるよ"),
            Template("つかれたね、{ownerCallName}。ゆっくりして", isShort = false),
            Template("よしよし、{firstPerson}がついてるよ", isShort = false)
        ),
        MessageCategory.LONELY to listOf(
            Template("{firstPerson}がいるよ！"),
            Template("さみしいときは{firstPerson}を呼んでね", isShort = false),
            Template("ずっとそばにいるからね", isShort = false)
        ),
        MessageCategory.MEAL to listOf(
            Template("ご飯？おやつもほしい！"),
            Template("{firstPerson}もお腹すいたな"),
            Template("ごはんの時間かな？わくわく", isShort = false)
        ),
        MessageCategory.PLAY to listOf(
            Template("遊ぼう！"),
            Template("{ownerCallName}、いっしょに遊ぼう！", isShort = false),
            Template("ボール持ってくるね！")
        ),
        MessageCategory.OUTING to listOf(
            Template("おかえり！待ってたよ"),
            Template("いってらっしゃい、{ownerCallName}"),
            Template("早く帰ってきてね！"),
            Template("おかえりなさい！うれしいな", isShort = false)
        ),
        MessageCategory.SLEEP to listOf(
            Template("おやすみ、{ownerCallName}"),
            Template("{firstPerson}も一緒に寝ようかな", isShort = false),
            Template("いい夢見てね")
        ),
        MessageCategory.WORK to listOf(
            Template("お仕事がんばって！"),
            Template("{ownerCallName}、無理しないでね", isShort = false),
            Template("帰りを待ってるよ")
        ),
        MessageCategory.SICK to listOf(
            Template("ちゃんと休んでね"),
            Template("むりしないでゆっくり休んでね", isShort = false),
            Template("{firstPerson}がそばにいるからね", isShort = false)
        ),
        MessageCategory.PRAISE to listOf(
            Template("えへへ、うれしいな"),
            Template("{ownerCallName}も大好きだよ！", isShort = false),
            Template("もっとなでて！")
        ),
        MessageCategory.EXPERT to listOf(
            Template("むずかしいことはわからないよ"),
            Template("それは{firstPerson}にはむずかしいな", isShort = false)
        )
        // UNKNOWN（判定不能）はテンプレートを持たず、常にAI経路（ReplyEngine）へ送る。
    )

    private val catTemplates: Map<MessageCategory, List<Template>> = mapOf(
        MessageCategory.GREETING to listOf(
            Template("……おはよう"),
            Template("やっときたのね"),
            Template("こんにちは。{firstPerson}は元気よ", isShort = false)
        ),
        MessageCategory.TIRED to listOf(
            Template("ゆっくり休みなよ"),
            Template("つかれてるの？そばにいてあげる", isShort = false),
            Template("無理しないでよね")
        ),
        MessageCategory.LONELY to listOf(
            Template("そばにいてあげてもいいよ"),
            Template("……ちょっとだけ甘えていいよ", isShort = false),
            Template("{firstPerson}がいるから平気でしょ", isShort = false)
        ),
        MessageCategory.MEAL to listOf(
            Template("おいしいものなら食べる"),
            Template("{firstPerson}にもごはんちょうだい", isShort = false),
            Template("おやつの気配がする…")
        ),
        MessageCategory.PLAY to listOf(
            Template("気が向いたら遊ぶね"),
            Template("今はいい気分だから遊んであげる", isShort = false),
            Template("ねこじゃらし持ってきて")
        ),
        MessageCategory.OUTING to listOf(
            Template("遅かったね。おかえり"),
            Template("いってらっしゃい。気をつけてね", isShort = false),
            Template("待っててあげたんだからね", isShort = false)
        ),
        MessageCategory.SLEEP to listOf(
            Template("{firstPerson}はもう寝るね"),
            Template("おやすみ。ちゃんと寝なさいよ", isShort = false),
            Template("隣で寝てあげる")
        ),
        MessageCategory.WORK to listOf(
            Template("がんばりなさいよね"),
            Template("{ownerCallName}のことは応援してる", isShort = false),
            Template("疲れすぎないでよ")
        ),
        MessageCategory.SICK to listOf(
            Template("無理しないでね"),
            Template("ちゃんとお医者さんに行きなさいよ", isShort = false),
            Template("{firstPerson}が看病してあげる", isShort = false)
        ),
        MessageCategory.PRAISE to listOf(
            Template("…まあ、悪くないわね"),
            Template("もっと褒めてもいいのよ"),
            Template("{ownerCallName}にしては上出来ね", isShort = false)
        ),
        MessageCategory.EXPERT to listOf(
            Template("それは人間に聞いてみて"),
            Template("{firstPerson}に聞かれてもわからないわ", isShort = false)
        )
        // UNKNOWN（判定不能）はテンプレートを持たず、常にAI経路（ReplyEngine）へ送る。
    )

    /** 性格ごとの追加バリエーション（一部カテゴリのみ。「尚良い」対応の範囲）。 */
    private val personalityBonus: Map<Triple<PetSpecies, MessageCategory, Personality>, List<Template>> = mapOf(
        Triple(PetSpecies.DOG, MessageCategory.TIRED, Personality.GENKI) to listOf(
            Template("つかれてなんかないよ！げんきげんき！", isShort = false)
        ),
        Triple(PetSpecies.DOG, MessageCategory.LONELY, Personality.SABISHIGARI) to listOf(
            Template("{ownerCallName}、行かないで…さみしいよ", isShort = false)
        ),
        Triple(PetSpecies.DOG, MessageCategory.MEAL, Personality.KUISHINBO) to listOf(
            Template("ごはん！ごはん！おなかぺこぺこ！", isShort = false)
        ),
        Triple(PetSpecies.DOG, MessageCategory.GREETING, Personality.AMAENBO) to listOf(
            Template("{ownerCallName}〜！なでてから話そ？", isShort = false)
        ),
        Triple(PetSpecies.CAT, MessageCategory.PRAISE, Personality.TSUNDERE) to listOf(
            Template("べ、別にうれしくなんかないんだから", isShort = false)
        ),
        Triple(PetSpecies.CAT, MessageCategory.GREETING, Personality.COOL) to listOf(
            Template("……ふん、来たのね")
        ),
        Triple(PetSpecies.CAT, MessageCategory.PLAY, Personality.MYPACE) to listOf(
            Template("今日はあんまり気分じゃないかも")
        ),
        Triple(PetSpecies.CAT, MessageCategory.MEAL, Personality.KUISHINBO) to listOf(
            Template("ごはんまだ？もう待てないんだけど", isShort = false)
        )
    )

    /** 種類・カテゴリ・性格・返信の長さ設定に応じたテンプレート候補の文字列一覧を返す。 */
    fun candidates(
        species: PetSpecies,
        category: MessageCategory,
        personality: Personality,
        replyLength: ReplyLength
    ): List<String> {
        val base = (if (species == PetSpecies.DOG) dogTemplates else catTemplates)[category].orEmpty()
        val bonus = personalityBonus[Triple(species, category, personality)].orEmpty()
        val all = base + bonus
        val filtered = if (replyLength == ReplyLength.SHORT) all.filter { it.isShort } else all
        val pool = filtered.ifEmpty { all }
        return pool.map { it.text }
    }
}
