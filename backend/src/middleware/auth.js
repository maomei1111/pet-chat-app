function requireAuth(req, res, next) {
  const secret = process.env.BACKEND_SHARED_SECRET || "";
  if (!secret) {
    return res.status(500).json({ error: "server_not_configured" });
  }
  const header = req.get("authorization") || "";
  if (header !== `Bearer ${secret}`) {
    return res.status(401).json({ error: "unauthorized" });
  }
  next();
}

module.exports = { requireAuth };
