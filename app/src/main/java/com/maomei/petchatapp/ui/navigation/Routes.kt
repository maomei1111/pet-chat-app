package com.maomei.petchatapp.ui.navigation

/** 画面遷移ルート定義（仕様書 3章「初期版の画面一覧」に対応）。 */
object Routes {
    const val SPLASH = "splash" // 1. 起動画面

    const val ONBOARDING_GRAPH = "onboarding_graph"
    const val ONBOARDING_START = "onboarding/start" // 2. ペット登録開始画面
    const val SPECIES_SELECT = "onboarding/species" // 3. ペット種類選択画面
    const val PET_INFO = "onboarding/info" // 4. ペット情報入力画面
    const val PHOTO_PICK = "onboarding/photo_pick" // 5. Googleフォト写真選択画面への遷移
    const val PHOTO_CONFIRM = "onboarding/photo_confirm" // 6. 選択写真確認画面
    const val PERSONALITY = "onboarding/personality" // 7. 性格・口調設定画面
    const val CONFIRM = "onboarding/confirm" // 8. 登録内容確認画面

    const val CHAT = "chat" // 9. メイン会話画面
    const val SETTINGS = "settings" // 10. ペット設定画面
}
