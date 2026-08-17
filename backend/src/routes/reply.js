const express = require("express");
const { requireAuth } = require("../middleware/auth");
const { generatePetReply } = require("../services/openaiReplyService");

const router = express.Router();

// 単一アプリからの利用を想定した簡易レート制限（仕様 4.7）。コスト暴走防止。
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

const VALID_SPECIES = ["DOG", "CAT"];
const VALID_REPLY_LENGTH = ["SHORT", "NORMAL"];
const MAX_USER_TEXT_LENGTH = 500;

router.post("/v1/reply", requireAuth, async (req, res) => {
  if (isRateLimited()) {
    return res.status(429).json({ error: "rate_limited" });
  }

  const body = req.body || {};
  const userText = typeof body.userText === "string" ? body.userText.trim() : "";
  const speciesInvalid = body.species !== undefined && !VALID_SPECIES.includes(body.species);
  const replyLengthInvalid =
    body.replyLength !== undefined && !VALID_REPLY_LENGTH.includes(body.replyLength);

  if (!userText || userText.length > MAX_USER_TEXT_LENGTH || speciesInvalid || replyLengthInvalid) {
    return res.status(400).json({ error: "invalid_input" });
  }

  const startedAt = Date.now();
  const result = await generatePetReply({
    species: body.species || "DOG",
    name: typeof body.name === "string" ? body.name : "うちの子",
    personality: typeof body.personality === "string" ? body.personality : "",
    firstPerson: typeof body.firstPerson === "string" ? body.firstPerson : "",
    ownerCallName: typeof body.ownerCallName === "string" ? body.ownerCallName : "",
    replyLength: body.replyLength || "SHORT",
    useEmoji: Boolean(body.useEmoji),
    userText,
  });
  const elapsedMs = Date.now() - startedAt;

  // 仕様 8: 本文・APIキー・Authorizationは出さず、成否と所要時間だけを記録する。
  console.log(
    `[ai_reply] ok=${result.ok} elapsedMs=${elapsedMs}${result.ok ? "" : ` reason=${result.reason}`}`
  );

  if (!result.ok) {
    return res.status(500).json({ error: "temporary_unavailable" });
  }

  return res.status(200).json({ reply: result.reply });
});

module.exports = router;
