const express = require("express");
const replyRouter = require("./routes/reply");

const app = express();
app.use(express.json({ limit: "10kb" }));

app.get("/health", (_req, res) => {
  res.status(200).json({ status: "ok" });
});

app.use("/", replyRouter);

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`pet-chat-app backend listening on port ${PORT}`);
});
