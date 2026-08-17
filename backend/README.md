# pet-chat-app backend

Androidアプリの「判定不能」カテゴリ入力に対して、OpenAI Responses APIを使ってペットらしい短い返信を生成するプロキシAPI。
OpenAIのAPIキーはこのサーバー側のみに置き、Androidアプリ（APK）には一切含めない。

## 構成

```
src/
├─ server.js                    # Expressエントリポイント、/health登録
├─ routes/reply.js               # POST /v1/reply（認証・入力検証・レート制限）
├─ services/openaiReplyService.js # OpenAI Responses API呼び出し・返信検証
├─ prompts/petReplyPrompt.js     # AIへのinstructions
└─ middleware/auth.js            # 共有シークレット検証
```

## 必要な環境変数（Railway）

| 変数 | 説明 |
|---|---|
| `OPENAI_API_KEY` | OpenAIのAPIキー。このサーバーにのみ設定する |
| `OPENAI_MODEL` | 使用するモデル名（例: `gpt-4o-mini`）。コードに固定せず環境変数で切り替える |
| `BACKEND_SHARED_SECRET` | Androidアプリとの共有シークレット。32文字以上のランダム値を推奨 |
| `PORT` | Railwayが自動的に設定する。ローカル実行時は未設定なら`8080` |

`OPENAI_API_KEY` と `BACKEND_SHARED_SECRET` はGitHubにコミットしない。

## エンドポイント

### `GET /health`

```json
{ "status": "ok" }
```

### `POST /v1/reply`

```http
Authorization: Bearer <BACKEND_SHARED_SECRET>
Content-Type: application/json
```

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

成功時 `200`:

```json
{ "reply": "無理しないでね。帰ったら遊ぼう！" }
```

エラー時: `400`（入力不正） / `401`（共有シークレット不正） / `429`（利用制限） / `500`（AI・サーバーエラー）

```json
{ "error": "invalid_input" }
```

## ローカル動作確認

```bash
npm install
OPENAI_API_KEY=sk-... OPENAI_MODEL=gpt-4o-mini BACKEND_SHARED_SECRET=test-secret PORT=8099 npm start
```

```bash
curl http://localhost:8099/health

curl -X POST http://localhost:8099/v1/reply \
  -H "Authorization: Bearer test-secret" \
  -H "Content-Type: application/json" \
  -d '{"species":"DOG","name":"ポチ","personality":"元気","firstPerson":"ぼく","ownerCallName":"パパ","replyLength":"SHORT","useEmoji":true,"userText":"今日は仕事に行きたくない"}'
```

## デプロイ（Railway）

```bash
cd backend
railway up --service <service-name>
railway variable set OPENAI_API_KEY=sk-... --service <service-name> --skip-deploys
railway variable set OPENAI_MODEL=gpt-4o-mini --service <service-name> --skip-deploys
railway variable set BACKEND_SHARED_SECRET=<32文字以上のランダム値> --service <service-name> --skip-deploys
railway redeploy --service <service-name> -y
```

環境変数は `railway variable set` の出力にそのまま表示されるため、ターミナル履歴やチャットに貼り付けない。
