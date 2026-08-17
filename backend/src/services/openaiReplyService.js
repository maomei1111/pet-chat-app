const OpenAI = require("openai");
const { PET_REPLY_INSTRUCTIONS } = require("../prompts/petReplyPrompt");

// 仕様書 7.3 と同じフォールバック文言。AI応答が検証に失敗した場合の置き換え用。
const FALLBACK_REPLIES = [
  "うまく言えないけど、そばにいるよ",
  "それはむずかしいなあ",
  "今日はゆっくりしよ？",
];

function pickFallback() {
  return FALLBACK_REPLIES[Math.floor(Math.random() * FALLBACK_REPLIES.length)];
}

const MAX_REPLY_LENGTH = 40;

/**
 * AIの返答をそのまま表示しないための検証（仕様 4.6）。
 * 文字列でない・空・長すぎる・改行過多の場合は null を返し、呼び出し元がフォールバックに置き換える。
 */
function sanitizeReply(rawText) {
  if (typeof rawText !== "string") return null;
  const collapsed = rawText.trim().replace(/\s*\n+\s*/g, " ").trim();
  if (!collapsed) return null;
  if (collapsed.length > MAX_REPLY_LENGTH) return null;
  return collapsed;
}

let cachedClient = null;
function getClient() {
  const apiKey = process.env.OPENAI_API_KEY || "";
  if (!apiKey) return null;
  if (!cachedClient) {
    cachedClient = new OpenAI({ apiKey });
  }
  return cachedClient;
}

/**
 * @param {object} payload 仕様 4.5 の input（species/name/personality/firstPerson/ownerCallName/replyLength/useEmoji/userText）
 * @returns {Promise<{ok: true, reply: string} | {ok: false, reason: string}>}
 */
async function generatePetReply(payload) {
  const client = getClient();
  const model = process.env.OPENAI_MODEL || "";
  if (!client || !model) {
    return { ok: false, reason: "not_configured" };
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 8000);

  try {
    const response = await client.responses.create(
      {
        model,
        instructions: PET_REPLY_INSTRUCTIONS,
        input: JSON.stringify(payload),
      },
      { signal: controller.signal }
    );

    const rawText = response.output_text;
    const cleaned = sanitizeReply(rawText);
    return { ok: true, reply: cleaned || pickFallback() };
  } catch (_err) {
    // OpenAIエラー・タイムアウトの詳細はログに出さず、呼び出し元へは一般化した理由だけ返す。
    return { ok: false, reason: "upstream_error" };
  } finally {
    clearTimeout(timeout);
  }
}

module.exports = { generatePetReply };
