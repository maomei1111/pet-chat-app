package com.maomei.petchatapp.data.model

/** 犬・猫の種類 */
enum class PetSpecies {
    DOG,
    CAT
}

/**
 * 性格。犬・猫で候補が一部重なる（甘えん坊・食いしん坊は共通）。
 * [ForDog] / [ForCat] は選択画面で候補を絞り込むための所属種別。
 */
enum class Personality(val displayName: String, val species: Set<PetSpecies>) {
    GENKI("元気", setOf(PetSpecies.DOG)),
    AMAENBO("甘えん坊", setOf(PetSpecies.DOG, PetSpecies.CAT)),
    OTTORI("おっとり", setOf(PetSpecies.DOG)),
    KUISHINBO("食いしん坊", setOf(PetSpecies.DOG, PetSpecies.CAT)),
    SABISHIGARI("寂しがり", setOf(PetSpecies.DOG)),
    TSUNDERE("ツンデレ", setOf(PetSpecies.CAT)),
    MYPACE("マイペース", setOf(PetSpecies.CAT)),
    COOL("クール", setOf(PetSpecies.CAT));

    companion object {
        fun candidatesFor(species: PetSpecies): List<Personality> =
            entries.filter { species in it.species }

        fun default(species: PetSpecies): Personality =
            if (species == PetSpecies.DOG) GENKI else TSUNDERE
    }
}

/** 返信の長さ設定 */
enum class ReplyLength(val displayName: String) {
    SHORT("短い"),
    NORMAL("普通")
}

/** 写真の取得元 */
enum class PhotoSource {
    GOOGLE_PHOTOS,
    SYSTEM_PICKER
}

/** メッセージの送信者 */
enum class Sender {
    USER,
    PET
}

/** 入力文の分類カテゴリ */
enum class MessageCategory {
    GREETING,
    TIRED,
    LONELY,
    MEAL,
    PLAY,
    OUTING,
    SLEEP,
    WORK,
    SICK,
    PRAISE,
    EXPERT,
    UNKNOWN
}

/** 返信の生成元 */
enum class ReplySource {
    TEMPLATE,
    AI,
    FALLBACK
}
