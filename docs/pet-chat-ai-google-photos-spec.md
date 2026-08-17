# AI返信・Googleフォト連携 実装仕様書

対象リポジトリ：`maomei1111/pet-chat-app`

## 1. 目的

現在のアプリに以下の2機能を追加する。

1. Railway上のバックエンドからOpenAI APIを呼び出し、判定不能な入力にAI返信を返す
2. Android標準Photo Pickerではなく、Google Photos Picker APIを使ってGoogleフォトから写真を選択する

既存のテンプレート返信、端末内保存、フォールバック返信は維持する。

## 2. 現在の実装を維持する部分

- `ReplyEngine`のテンプレート優先処理
- `MessageClassifier`によるカテゴリ分類
- `ReplyTemplates`の犬・猫別返信
- `Room`によるペット・写真・会話履歴保存
- 写真の端末内コピー
- 40文字以内への返信整形
- AI通信失敗時のフォールバック返信
- `BackendAiReplyProvider`から`POST /v1/reply`を呼ぶ構成

## 3. 完成後の構成

```text
Androidアプリ
  ├─ 定型カテゴリ入力 → 端末内テンプレート返信
  └─ 判定不能入力
       ↓
     Railway Backend
       ↓
     OpenAI Responses API
       ↓
     短い犬・猫らしい返信
```

Googleフォト連携は次の構成とする。

```text
Androidアプリ
  ↓ OAuthアクセストークン取得
Google Photos Picker API
  ↓ pickerUriをGoogleフォトで開く
ユーザーが写真選択
  ↓ セッションをポーリング
選択済みmediaItems取得
  ↓ 一時URLから画像をダウンロード
アプリ専用領域へ保存
```

## 4. AI返信仕様

### 4.1 返信経路

`app/src/main/java/com/maomei/petchatapp/reply/ReplyEngine.kt`の現在の優先順位を維持する。

1. 入力文をカテゴリ分類
2. テンプレート候補があれば端末内で返信
3. `UNKNOWN`などテンプレートがない場合だけ`AiReplyProvider`を呼び出す
4. AIが返信できなければフォールバック返信

通常の「疲れた」「ご飯」「遊ぼう」などはAIを呼び出さない。

### 4.2 Railway環境変数

Railwayには少なくとも以下を設定する。

```text
OPENAI_API_KEY=OpenAI APIキー
OPENAI_MODEL=使用するモデル名
BACKEND_SHARED_SECRET=Androidアプリとバックエンド間の共有シークレット
PORT=Railwayが提供するPORTを使用
```

注意：

- `OPENAI_API_KEY`はRailwayだけに置く
- Androidプロジェクト、GitHub、APK、`local.properties`以外の公開ファイルにAPIキーを入れない
- `BACKEND_SHARED_SECRET`もGitHubへコミットしない
- RailwayのログにAPIキーやリクエスト本文を出力しない
- 実際に利用できるモデル名はOpenAI APIで利用可能なものを設定する
- モデル名はコードに固定せず、`OPENAI_MODEL`で変更可能にする

OpenAI公式ドキュメントでは、テキスト生成にはResponses APIを使用する構成が案内されている。APIキーはサーバー側の環境変数から読み込み、AndroidアプリからOpenAIへ直接アクセスしない。[OpenAI公式：Text generation](https://developers.openai.com/api/docs/guides/text)

### 4.3 APIエンドポイント

#### `POST /v1/reply`

Android側の既存`BackendAiReplyProvider`と互換にする。

リクエスト：

```json
{
  "species": "DOG",
  "name": "ポチ",
  "personality": "元気",
  "firstPerson": "ぼく",
  "ownerCallName": "パパ",
  "replyLength": "SHORT",
  "useEmoji": true,
  "userText": "今日は仕事に行きたくない"
}
```

レスポンス成功時：

```json
{
  "reply": "無理しないでね。帰ったら遊ぼう！"
}
```

エラー時：

```json
{
  "error": "temporary_unavailable"
}
```

HTTPステータス：

- `200`：返信成功
- `400`：入力不正
- `401`：共有シークレット不正
- `429`：利用制限
- `500`：AIまたはサーバーエラー

### 4.4 認証

Android側の既存実装に合わせる。

```http
Authorization: Bearer ${BACKEND_SHARED_SECRET}
Content-Type: application/json
```

Railway側では、Authorizationヘッダーが一致しない場合はAIを呼ばず、`401`を返す。

共有シークレットは最低32文字以上のランダム値を使用する。

### 4.5 AIプロンプト

プロンプトはバックエンドのソースコードで管理し、Railway環境変数やOpenAIの保存済みプロンプトに依存しない。

`instructions`：

```text
あなたは、ユーザーが飼っている犬または猫です。
人間の専門家ではなく、飼い主を癒やすためのペットとして返答してください。

必ず次のルールを守ってください。
- 日本語で返答する
- 原則40文字以内にする
- 原則1〜2文にする
- 犬または猫らしい、簡単で自然な言葉を使う
- 飼い主の気持ちに寄り添う
- 難しい質問には「むずかしいことはわからないよ」などと返す
- 医療、法律、金融、仕事の判断を断定しない
- 病気や薬について診断・指示をしない
- 不安を煽らない
- 攻撃的、性的、差別的な内容を返さない
- ペットが人間のような専門知識を持つ表現をしない
- 返信本文だけを返し、説明・箇条書き・前置きを付けない
```

`input`には、ペット設定とユーザー入力をJSON形式で渡す。

```json
{
  "species": "DOG",
  "name": "ポチ",
  "personality": "元気",
  "firstPerson": "ぼく",
  "ownerCallName": "パパ",
  "replyLength": "SHORT",
  "useEmoji": true,
  "userText": "今日は仕事に行きたくない"
}
```

### 4.6 AIレスポンス検証

AIの返答をそのまま表示しない。

バックエンドで以下を検証する。

- 文字列であること
- 空文字でないこと
- 40文字以内であること
- 改行を過剰に含まないこと
- `reply`以外の説明文を含まないこと

40文字を超えた場合は、文末を不自然に切らず、短いフォールバック返信へ置き換える。

Android側の`ReplyEngine`にも既存の40文字制限を残す。

### 4.7 利用回数制限

初期版では、次のいずれかを実装する。

- 1端末あたり1日20回
- Railway側でIPまたは共有シークレット単位の簡易レート制限

ログインがないため、端末IDを完全なユーザー認証として扱わない。

### 4.8 エラーハンドリング

Android側では以下の場合にAI失敗として扱う。

- 8秒以内に接続できない
- HTTPステータスが200以外
- JSONに`reply`がない
- `reply`が空文字
- APIキー未設定
- Railwayが停止中

この場合は既存のフォールバック返信を表示する。

## 5. Railwayバックエンド実装

### 5.1 推奨構成

```text
backend/
├─ package.json
├─ package-lock.json
├─ src/
│  ├─ server.js
│  ├─ routes/reply.js
│  ├─ services/openaiReplyService.js
│  ├─ prompts/petReplyPrompt.js
│  └─ middleware/auth.js
└─ README.md
```

Node.js + Expressを使用する。

### 5.2 実装要件

- `express.json()`でJSONを受け取る
- `POST /v1/reply`を実装する
- `GET /health`を実装する
- `Authorization: Bearer ...`を検証する
- `OPENAI_API_KEY`を環境変数から読む
- OpenAI Responses APIを呼び出す
- OpenAIレスポンスからテキストを抽出する
- AIエラーをAndroidへそのまま返さず、一般化したエラーにする
- 本文、APIキー、Authorizationヘッダーをログに出さない
- Railwayの`PORT`を使用する

### 5.3 ヘルスチェック

```http
GET /health
```

成功時：

```json
{
  "status": "ok"
}
```

`OPENAI_API_KEY`の値はレスポンスに含めない。

### 5.4 OpenAI呼び出し

Node.jsのOpenAI公式SDKまたは公式HTTP APIを使用する。

Responses APIの概念例：

```javascript
const response = await client.responses.create({
  model: process.env.OPENAI_MODEL,
  instructions: PET_REPLY_INSTRUCTIONS,
  input: JSON.stringify(payload)
});

const reply = response.output_text;
```

実際のSDKバージョンに合わせてAPI形式を確認し、`output[0]`だけを決め打ちして解析しない。

## 6. Android側AI設定

### 6.1 `local.properties`

Git管理外の`local.properties`に設定する。

```properties
backend.base.url=https://<railway-service>.up.railway.app
backend.shared.secret=<Railwayと同じ共有シークレット>
```

末尾に`/`を付けても動作するよう、既存の`trimEnd('/')`を維持する。

### 6.2 確認方法

1. Railwayで`GET /health`を確認
2. Railwayで`POST /v1/reply`を手動確認
3. Androidの`local.properties`にURLと共有シークレットを設定
4. Android Studioで再ビルド
5. 判定不能な文章を送信
6. チャットメッセージの`replySource`が`AI`になることを確認

テンプレートに該当する入力ではAIを呼ばないため、AI動作確認には次のような入力を使う。

- 「もし明日、別の町に引っ越したらどう思う？」
- 「最近いろいろ考えすぎて眠れない」
- 「人生って何だと思う？」

## 7. Google Photos Picker API仕様

### 7.1 現在の暫定実装

現在は以下を使っている。

```kotlin
ActivityResultContracts.PickMultipleVisualMedia
```

これはAndroid標準Photo Pickerであり、Google Photos Picker APIではない。

暫定実装は残し、正式連携に失敗した場合のフォールバックとして利用できるようにする。

### 7.2 正式連携の前提

- Google Cloudプロジェクトを用意する
- Google Photos Picker APIを有効化する
- OAuth同意画面を設定する
- Android用OAuthクライアントを作成する
- `https://www.googleapis.com/auth/photospicker.mediaitems.readonly`を使用する
- 写真選択の目的を同意画面・アプリ画面に明示する

Google Photos Picker APIは、セッションを作成し、返された`pickerUri`をGoogleフォトで開き、セッションをポーリングして、選択済みメディアを取得する流れで動作する。[Google公式：Get started with the Picker API](https://developers.google.com/photos/picker/guides/get-started-picker)

### 7.3 Picker処理フロー

1. OAuthアクセストークンを取得
2. Pickerセッション作成
3. レスポンスから`sessionId`と`pickerUri`を取得
4. `pickerUri`を外部ブラウザまたはGoogleフォトで開く
5. アプリへ戻った後、セッション状態をポーリング
6. `mediaItemsSet == true`になるまで待つ
7. 選択済みメディア一覧を取得
8. 画像メディアだけを抽出
9. `baseUrl`から画像をダウンロード
10. `context.filesDir/pet_photos/`へ保存
11. `PetPhotoEntity`をRoomへ保存
12. セッションを削除

### 7.4 Google Photos APIクラス構成

既存の抽象を維持し、以下を追加する。

```text
data/photo/
├─ PhotoPickerService.kt
├─ SystemPhotoPickerService.kt
├─ GooglePhotosPickerService.kt
├─ GooglePhotosApiClient.kt
├─ GoogleOAuthService.kt
└─ GooglePickerSession.kt
```

`AppContainer`では、設定値に応じて正式版と暫定版を切り替えられるようにする。

```kotlin
val photoPickerService: PhotoPickerService by lazy {
    if (googlePhotosEnabled) {
        GooglePhotosPickerService(...)
    } else {
        SystemPhotoPickerService(appContext)
    }
}
```

### 7.5 セッションポーリング

- 固定間隔ではなく、APIレスポンスの推奨ポーリング間隔を使用する
- タイムアウト時間を超えたらキャンセル扱いにする
- アプリがバックグラウンドへ移動した場合はポーリングを停止または再開可能にする
- 成功後は選択済みメディア取得後にセッションを削除する
- 失敗時はユーザーに「写真を取得できませんでした」と表示する

### 7.6 URL・画像保存

Google Photos APIの`baseUrl`は永続URLとして保存しない。取得用URLには有効期限があるため、選択時に端末内へコピーする。

Roomには以下を保存する。

- `localPath`
- `googleMediaItemId`（任意）
- `source = GOOGLE_PHOTOS`
- `isMain`

### 7.7 写真選択UI

既存ボタン文言は維持する。

```text
Googleフォトから写真を選ぶ
```

正式Pickerを使用する場合、ユーザーには以下を表示する。

```text
Googleフォトを開いて、うちの子の写真を選択してください。
選択した写真だけをアプリ内に保存します。
```

写真選択キャンセル時はエラー画面にせず、選択画面へ戻す。

## 8. データモデル変更

### PetPhoto

`PhotoSource`に以下を追加する。

```kotlin
enum class PhotoSource {
    SYSTEM_PICKER,
    GOOGLE_PHOTOS
}
```

### AI利用ログ

初期版では、少なくとも以下を保存またはデバッグログで確認できるようにする。

- `replySource`: `TEMPLATE` / `AI` / `FALLBACK`
- カテゴリ
- AI呼び出し日時
- 成否
- HTTPステータス
- レスポンス時間

APIキー、共有シークレット、ユーザーの全文入力はログに保存しない。

## 9. 実装順序

### PR6：Railway AIバックエンド

- `backend/`にNode.js + Express構成を追加
- `/health`追加
- `/v1/reply`追加
- 共有シークレット認証
- OpenAI Responses API接続
- 犬猫用プロンプト追加
- 40文字制限・返信検証
- エラー・タイムアウト処理
- Railwayデプロイ設定

### PR7：Android AI接続確認

- `local.properties`設定手順をREADMEへ追加
- AI接続成功時の`replySource = AI`確認
- AI失敗時のフォールバック確認
- テンプレート返信ではAIを呼ばないことを確認
- AI利用ログ追加

### PR8：Google OAuth基盤

- Google Cloud設定手順を文書化
- OAuth同意画面設定
- Android OAuthクライアント設定
- 必要スコープの取得
- トークン更新・失効処理

### PR9：Google Photos Picker API

- Pickerセッション作成
- pickerUri表示・起動
- セッションポーリング
- 選択メディア一覧取得
- 画像ダウンロード
- 端末内保存
- Room登録
- セッション削除

### PR10：Googleフォト連携品質改善

- キャンセル処理
- タイムアウト処理
- 通信エラー処理
- 既存写真との重複判定
- 画像サイズ・容量制限
- Android標準Photo Pickerへのフォールバック

## 10. 受け入れ条件

### AI

- Railwayの`GET /health`が200を返す
- 正しい共有シークレットで`POST /v1/reply`が200を返す
- 不正な共有シークレットで401になる
- RailwayにAPIキーを設定した状態でAI返信が返る
- Androidで判定不能入力を送るとAI返信が表示される
- AI返信の`replySource`が`AI`になる
- テンプレート対象入力ではAIを呼び出さない
- AIエラー時にフォールバック返信が表示される
- AI返信が40文字を超えない
- APIキーがAPKやGitHubに含まれていない
- RailwayログにAPIキーが表示されない

### Googleフォト

- Googleアカウント認証が開始できる
- Google Photos Picker APIのセッションを作成できる
- Googleフォトの選択画面が開く
- 複数写真を選択できる
- 選択完了を検知できる
- 選択した画像だけを取得できる
- 取得画像が端末内に保存される
- アプリ再起動後も写真が表示される
- 写真を削除すると端末内ファイルも削除される
- Pickerキャンセル時にアプリが異常終了しない
- セッションタイムアウト時に再試行できる
- Googleフォト連携失敗時に標準Photo Pickerへ切り替えられる

## 11. セキュリティ・公開時の注意

- OpenAI APIキーをAndroidアプリへ入れない
- APIキーをGitHubへコミットしない
- Railway環境変数をREADMEやIssueに貼らない
- Google OAuthアクセストークンをログへ出さない
- Googleフォトの画像を必要以上に保持しない
- 選択写真の削除機能を用意する
- プライバシーポリシーにAI送信内容、Googleフォト利用、保存場所を記載する
- AI返信は娯楽目的であり、医療・法律・金融の助言ではないことを明記する

## 12. 開発時の確認コマンド例

Railwayの公開URLを`https://example.up.railway.app`とした場合：

```powershell
Invoke-WebRequest `
  -Uri "https://example.up.railway.app/health" `
  -Method Get
```

AI返信確認例：

```powershell
$headers = @{
  Authorization = "Bearer <共有シークレット>"
  "Content-Type" = "application/json"
}

$body = '{
  "species":"DOG",
  "name":"ポチ",
  "personality":"元気",
  "firstPerson":"ぼく",
  "ownerCallName":"パパ",
  "replyLength":"SHORT",
  "useEmoji":true,
  "userText":"今日は仕事に行きたくない"
}'

Invoke-RestMethod `
  -Uri "https://example.up.railway.app/v1/reply" `
  -Method Post `
  -Headers $headers `
  -Body $body
```

## 13. 完了後の期待状態

```text
backend/
  Railwayで起動するAIプロキシAPI

app/
  テンプレート返信
  Railway AI返信
  Google Photos Picker API
  Android標準Photo Pickerフォールバック
  Room保存
```

最終的に、通常の短い会話は端末内テンプレートで低コストに処理し、テンプレートで処理できない文章だけRailway経由でAIに送信する。

