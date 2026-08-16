const express = require("express");

const app = express();
app.use(express.json({ limit: "10kb" }));

const PORT = process.env.PORT || 8080;
const OPENAI_API_KEY = process.env.OPENAI_API_KEY || "";
const OPENAI_MODEL = process.env.OPENAI_MODEL || "gpt-4o-mini";
const APP_SHARED_SECRET = process.env.APP_SHARED_SECRET || "";

// 単一アプリからの利用を想定した簡易レート制限（コスト暴走防止）。
const WINDOW_MS = 60_000;
const MAX_REQUESTS_PER_WINDOW = 30;
let windowStart = Date.now();
let windowCount = 0;

function isRateLimited() {
  const now = Date.now();
  if (now - windowStart > WINDOW_MS) {
    windowStart = now;
    windowCount = 0;
  }
  windowCount += 1;
  return windowCount > MAX_REQUESTS_PER_WINDOW;
}

function requireAuth(req, res, next) {
  if (!APP_SHARED_SECRET) {
    // 共有シークレット未設定のデプロイは誰でも叩けてしまうため起動を拒否する運用が望ましいが、
    // ここではリクエストを拒否するだけに留める。
    return res.status(500).json({ reply: null, error: "server not configured" });
  }
  const header = req.get("authorization") || "";
  const expected = `Bearer ${APP_SHARED_SECRET}`;
  if (header !== expected) {
    return res.status(401).json({ reply: null, error: "unauthorized" });
  }
  next();
}

const SPECIES_LABEL = { DOG: "犬", CAT: "猫" };
const LENGTH_LABEL = { SHORT: "短い（20文字程度）", NORMAL: "普通（40文字以内）" };

function buildSystemPrompt(body) {
  const speciesLabel = SPECIES_LABEL[body.species] || "犬";
  const lengthLabel = LENGTH_LABEL[body.replyLength] || LENGTH_LABEL.SHORT;
  const emojiLabel = body.useEmoji ? "使用する" : "使用しない";

  return `あなたはユーザーが飼っている犬または猫です。
人間の専門家ではありません。
飼い主に寄り添う、短く自然なペットらしい返事をしてください。
返答は原則40文字以内、1〜2文にしてください。
難しい質問、医療、法律、金融、仕事の判断には断定的に答えず、
「むずかしいことはわからないよ」など、ペットらしく返してください。
犬や猫が実際にできない高度な知識を持つような回答は禁止です。
不安を煽る表現、攻撃的な表現、極端な表現は使わないでください。
返信本文だけを出力し、説明や記号での装飾は付けないでください。

[ペット情報]
種類: ${speciesLabel}
名前: ${body.name || "うちの子"}
性格: ${body.personality || ""}
一人称: ${body.firstPerson || ""}
飼い主の呼び方: ${body.ownerCallName || ""}
返信の長さ設定: ${lengthLabel}
絵文字設定: ${emojiLabel}（絵文字はこちらでは付けないでください。呼び出し元で付与します）`;
}

app.get("/", (_req, res) => {
  res.status(200).send("pet-chat-app backend: ok");
});

app.post("/v1/reply", requireAuth, async (req, res) => {
  if (isRateLimited()) {
    return res.status(429).json({ reply: null, error: "rate limited" });
  }

  const userText = typeof req.body?.userText === "string" ? req.body.userText.trim() : "";
  if (!userText || userText.length > 500 || !OPENAI_API_KEY) {
    return res.status(200).json({ reply: null });
  }

  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);

    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${OPENAI_API_KEY}`,
      },
      body: JSON.stringify({
        model: OPENAI_MODEL,
        messages: [
          { role: "system", content: buildSystemPrompt(req.body) },
          { role: "user", content: userText },
        ],
        max_tokens: 60,
        temperature: 0.8,
      }),
      signal: controller.signal,
    });
    clearTimeout(timeout);

    if (!response.ok) {
      return res.status(200).json({ reply: null });
    }

    const data = await response.json();
    const reply = data?.choices?.[0]?.message?.content?.trim();
    return res.status(200).json({ reply: reply || null });
  } catch (_err) {
    return res.status(200).json({ reply: null });
  }
});

app.listen(PORT, () => {
  console.log(`pet-chat-app backend listening on port ${PORT}`);
});
