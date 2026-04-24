# 如何使用 (How to Use)

本文件將引導您完成 Pollo-Mail CLI 的安裝、環境配置與基本操作指令。

## 📥 安裝與執行

### 方案 A：使用 Native Image 原生執行檔 (推薦)
如果您已經擁有了編譯好的二進位執行檔（或透過自行編譯取得），您可以直接將其搬移到您的環境變數路徑下：

```bash
# 假設您剛編譯完畢
sudo cp build/native/nativeCompile/pollo-mail-cli /usr/local/bin/pollo-mail
sudo chmod +x /usr/local/bin/pollo-mail

# 測試執行
pollo-mail --help
```

### 方案 B：使用 Gradle (開發與測試環境)
若您要在原始碼環境下直接執行，請確保您的環境擁有 **Java 21**：

```bash
export JAVA_HOME=/path/to/your/jdk21
./gradlew bootRun --args="[指令與參數]"
```

---

## ⚙️ 環境與帳號配置 (`setup`)

第一次使用前，請務必執行 `setup` 指令。
這會啟動一個互動式精靈，協助您設定 SMTP 與 IMAP 伺服器資訊。

```bash
pollo-mail setup
```

**互動提示範例：**
*   **SMTP Host**: `smtp.gmail.com`
*   **SMTP Port**: `587` (或 `465`)
*   **SMTP Username**: `your_email@gmail.com`
*   **SMTP Password**: 您的密碼 *(註：Google 等服務可能需要至後台申請「應用程式專用密碼 (App Passwords)」)*

完成後，您的設定檔將會被**加密儲存**在 `~/.config/pollo-mail/config.yaml`。
> ⚠️ **注意**：這份檔案的加密金鑰綁定了您本機的硬體 MAC Address，請勿將其複製到其他電腦上使用，這將導致解密失敗。

---

## 📨 發送郵件 (`send`)

設定完成後，您可以使用 `send` 指令快速發信。

### 基本指令 (純文字發信)
```bash
pollo-mail send -t recipient@example.com -s "信件主旨" -b "這是信件內文"
```

### 群發與 HTML 格式
使用逗號 `,` 分隔多個收件人，並加上 `--html` 讓內文以 HTML 格式呈現：
```bash
pollo-mail send -t a@example.com,b@example.com \
                -s "本週進度報告" \
                -b "<h1>進度更新</h1><p>請查閱最新報告。</p>" \
                --html
```

### 夾帶附件
使用 `-a` 或 `--attach` 參數可以附加多個檔案：
```bash
pollo-mail send -t boss@example.com \
                -s "請款單與發票" \
                -b "附件為本月請款明細" \
                -a ./invoice.pdf \
                -a ./receipt.jpg
```

---

## 📥 接收郵件並匯出 Markdown (`fetch`)

專為個人知識管理 (PKM) 設計的功能。它會透過 IMAP 協定下載信件，並自動過濾冗餘的 HTML 標籤，轉譯為乾淨的 Markdown 筆記。

### 基本指令 (抓取最新信件)
```bash
# 預設抓取最新的 10 封信，儲存在 ~/.pollo-mail/inbox/
pollo-mail fetch
```

### 指定數量與未讀過濾
```bash
# 抓取最新 5 封
pollo-mail fetch -n 5

# 只抓取「未讀」的信件
pollo-mail fetch --unread-only
```

### 指定匯出目錄
您可以直接將匯出目錄指向您的 Obsidian 或 Logseq Vault：
```bash
pollo-mail fetch -n 3 -o ~/Documents/MyWiki/Inbox/
```

**匯出格式範例**：
執行後，系統會為每封信建立一個獨立的資料夾（資料夾名稱包含日期與主旨），內部會有 `email.md` 以及夾帶的實體附件檔案。
`email.md` 的頂端會自動生成標準的 YAML Frontmatter：

```markdown
---
date: 2026-04-24 15:30:00
from: "sender@example.com"
to: "you@example.com"
subject: "重要通知"
---

信件純文字內容...

---
**Attachments:**
- [document.pdf](./document.pdf)
```
