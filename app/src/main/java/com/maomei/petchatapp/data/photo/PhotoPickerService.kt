package com.maomei.petchatapp.data.photo

import android.net.Uri
import com.maomei.petchatapp.data.model.PetPhoto

/**
 * 写真取り込みサービスの抽象。
 *
 * 仕様書 4.4 では Google Photos Picker API を使う想定だが、実際の連携には
 * プロジェクトオーナーが用意する Google Cloud OAuth クライアント資格情報が必要であり、
 * このタスクでは用意できない。そのため暫定実装として [SystemPhotoPickerService] を用意し、
 * Android 標準の System Photo Picker (`PickMultipleVisualMedia`) で選択された画像を
 * アプリ専用の内部ストレージへコピーする「体験としては仕様と同じ」実装とする。
 *
 * 将来、本物の Google Photos Picker API 実装に差し替える際はこのインターフェースの
 * 実装クラスを追加し、[com.maomei.petchatapp.di.AppContainer] での紐付けを変更するだけでよい。
 */
interface PhotoPickerService {
    /**
     * 選択済みの URI 一覧を受け取り、アプリ専用領域へコピーして [PetPhoto] のリストを返す。
     * 呼び出し元（Compose 側）が Activity Result API で URI を取得する部分を担当し、
     * このメソッドはコピー・永続化のみを担当する。
     */
    suspend fun importPhotos(petId: String, uris: List<Uri>): List<PetPhoto>

    /** アプリ専用領域に保存済みの写真ファイルを削除する。 */
    suspend fun deletePhotoFile(localPath: String)
}
