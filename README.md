# Pollo-Mail CLI 🐔✉️

**Pollo-Mail CLI** 是一個專為終端機重度使用者與筆記愛好者打造的跨平台電子郵件工具。

## 🎯 解決什麼問題 (The "Why")

在終端機環境中，傳統的 CLI 電子郵件工具往往面臨以下痛點：
1. **安全性堪憂**：大多需要將 SMTP/IMAP 密碼明文寫死在腳本或環境變數中。
2. **與現代筆記軟體脫節**：收下來的信件難以直接匯入 Obsidian、Logseq 等使用 Markdown 的知識庫軟體中。
3. **啟動緩慢**：如果使用 Java 開發，冷啟動 (Cold Start) 動輒 1~2 秒，對於一個需要隨敲即用的命令列工具來說體驗極差。

**Pollo-Mail CLI 完美解決了上述問題：**

*   **🔒 硬體級別的安全防護**：我們不儲存明文密碼。工具會自動擷取您電腦的 MAC Address 與作業系統名稱作為獨一無二的 Salt，並採用 AES-256 對您的信箱設定檔進行本地加密。設定檔一旦離開您的電腦，就形同廢紙。
*   **📝 為 PKM (個人知識管理) 而生**：內建強大的信件解析器，能自動將繁雜的 HTML 郵件轉化為乾淨的 Markdown 格式，並在檔頭附上 `YAML Frontmatter` (包含日期、寄件人、主旨等 Metadata)。附件也會被整齊地下載封裝，無縫對接您的第二大腦。
*   **⚡ 顛覆認知的極致速度**：藉由 GraalVM Native Image 的 AOT 靜態編譯技術，我們將龐大的 Java 與 Spring Boot 框架壓縮成單一且無需 JRE 的二進位執行檔。啟動速度來到驚人的 **~22 毫秒**，給您純 C/Rust 語言等級的極速體驗。

如果您希望在終端機內優雅、安全且極速地收發郵件，同時將重要的郵件內容無縫轉化為個人知識庫的一部分，Pollo-Mail CLI 就是為您量身打造的完美解決方案。

---

## 📂 文件導覽

關於如何安裝、使用或是參與開發，請參考以下文件：

*   🚀 **[如何使用 (SETUP.md)](docs/SETUP.md)**：包含安裝說明、帳號配置 (`setup`)、發送 (`send`) 與接收 (`fetch`) 郵件的完整指令教學。
*   🛠️ **[如何開發 (DEVELOP.md)](docs/DEVELOP.md)**：包含本專案的系統架構、GraalVM Native Image 編譯指南與 Gradle 環境設定。
*   📝 **[架構決策 (ADR)](docs/adr/)**：記錄了開發過程中的重大架構決策與選型考量。

---
*Built with ❤️ utilizing GraalVM & Spring Boot 3*