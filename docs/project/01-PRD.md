# 產品需求文件 (PRD)：Pollo-Mail CLI

## 1. 專案概述 (Overview)
Pollo-Mail CLI 是一款基於 Java 21 與 GraalVM Native Image 技術開發的跨平台原生命令列郵件工具。旨在提供開發者與系統管理員一個啟動極快、無須依賴本地 JRE 環境的郵件收發方案，完美適配 CI/CD 流程、系統監控警報與日常自動化腳本需求。

## 2. 目標受眾與使用場景 (Target Audience & Use Cases)
*   **系統管理員 (SysAdmins) / DevOps 工程師**：需要在 Bash/Shell 腳本中快速發送伺服器狀態報告或監控告警。
*   **開發人員 (Developers)**：在 CI/CD 流水線 (如 GitHub Actions) 構建完成或失敗時，發送包含測試報告附件的通知信。
*   **終端機重度使用者**：習慣在 CLI 環境下工作，希望透過簡單的指令快速檢查最新郵件或發送純文字/HTML 郵件。

## 3. 使用者故事 (User Stories)

### Epic 1: 配置與環境管理 (Configuration)
*   **US-1.1**: 作為一名使用者，我希望能在 `~/.config/pollo-mail/config.yaml` 中統一設定 SMTP 與 IMAP 伺服器資訊，這樣我就不用在每次下指令時重複輸入伺服器位置。
*   **US-1.2**: 作為一名新使用者，我希望能有一個互動式的設定指令 (如 `pollo-mail setup`) 引導我一步步輸入伺服器與帳號密碼資訊，並自動產生設定檔。
*   **US-1.3**: 作為注重安全的使用者，我希望儲存在設定檔中的密碼是以對稱加密且加鹽 (Salted) 的方式存放，而「鹽」的資訊可以綁定主機固定的特徵，確保即使設定檔外洩，在其他機器上也無法輕易解開。

### Epic 2: 發送郵件 (Send Email)
*   **US-2.1**: 作為一名使用者，我希望使用 `pollo-mail send -t user@example.com -s "主旨" -b "內文"` 指令發送純文字郵件。
*   **US-2.2**: 作為一名使用者，我希望透過 `--html` 標籤發送 HTML 格式的郵件，讓系統通知信的排版更易讀。
*   **US-2.3**: 作為一名 DevOps 工程師，我希望發送郵件時可以使用 `-a /path/to/report.pdf` 夾帶單個或多個附件，以便傳送日誌或報表。

### Epic 3: 接收與檢視郵件 (Fetch Email)
*   **US-3.1**: 作為一名終端機使用者，我希望使用 `pollo-mail fetch` 快速列出收件匣最新的郵件標題、寄件人與時間。
*   **US-3.2**: 作為一名使用者，我希望透過 `--limit 5` 參數限制抓取的郵件數量，避免終端機被大量舊信件洗版。
*   **US-3.3**: 作為一名使用者，我希望在 `fetch` 指令中加入特定參數 (例如透過郵件 ID 或 `--read`) 來檢視單封郵件的詳細內文。
*   **US-3.4**: 作為一名使用者，我希望能夠將抓取下來的信件資料儲存為 Markdown (.md) 格式檔案，方便我使用其他純文字工具或筆記軟體進行管理與備份。

## 4. 功能規格與驗收標準 (Acceptance Criteria)

### 4.1 設定檔規格 (Configuration Schema)
*   **檔案位置**：預設為 `~/.config/pollo-mail/config.yaml`。
*   **AC 驗收標準**：
    *   [ ] 系統啟動時若找不到設定檔，應提示友善的錯誤訊息並引導使用者執行 `pollo-mail setup` 指令進行初始化。
    *   [ ] 支援解析巢狀 YAML 結構區分 `smtp` 與 `imap` 設定。
    *   [ ] 密碼儲存與解析機制：密碼直接儲存於設定檔中。寫入時必須使用綁定主機特徵（如 Machine ID 或特定硬體資訊）作為 Salt 進行對稱加密（例如 AES-256）。程式讀取時能自動利用主機特徵進行解密還原。

### 4.2 CLI 命令結構與參數
採用 Picocli 實作，需支援 `--help` 查看說明。

#### 子命令：`setup` (互動式配置)
*   **語法**：`pollo-mail setup`
*   **AC 驗收標準**：
    *   [ ] 啟動互動式問答流程，依序提示輸入 SMTP/IMAP 的 host, port, username, password 等資訊。
    *   [ ] 接收密碼輸入時，終端機需隱藏字元。
    *   [ ] 將輸入的密碼進行「本機加鹽對稱加密」處理後，自動產生或更新 `~/.config/pollo-mail/config.yaml` 檔案。

#### 子命令：`send`
*   **語法**：`pollo-mail send [OPTIONS]`
*   **AC 驗收標準**：
    *   [ ] 缺少必填參數 `-t` (`--to`) 時，需顯示錯誤提示。
    *   [ ] 支援 `-s` (`--subject`), `-b` (`--body`) 參數。
    *   [ ] 支援多重 `-a` (`--attach`) 參數載入檔案。若檔案路徑無效，需提前報錯中斷。
    *   [ ] 支援多個收件人（以逗號分隔）。
    *   [ ] 成功發送後，終端機輸出成功訊息及耗時。

#### 子命令：`fetch`
*   **語法**：`pollo-mail fetch [OPTIONS]`
*   **AC 驗收標準**：
    *   [ ] 預設顯示最新 10 封郵件的列表（包含 ID, Date, From, Subject）。
    *   [ ] `--limit <N>` 參數必須為正整數，若輸入非數字需拋出 Picocli 錯誤。
    *   [ ] 處理 IMAP 連線逾時需有明確的重試或錯誤提示。
    *   [ ] 支援將信件儲存為 Markdown 格式檔案 (可透過類似 `--export <DIR>` 參數或特定指令觸發)。Markdown 檔案頂部需包含 YAML Frontmatter (記錄 `From`, `To`, `Date`, `Subject`, `Content` 等 Meta 資訊)，而信件本文則儲存於檔案主體。

## 5. 非功能需求 (Non-Functional Requirements)

*   **NFR-1 效能**：編譯為 GraalVM Native Image 後，在不涉及網路請求的空載執行（例如 `pollo-mail --help`）時，啟動時間應小於 50ms。
*   **NFR-2 跨平台**：需透過 GitHub Actions 產出 Windows (`.exe`), macOS (`arm64`/`x86_64`), Linux (`elf`) 的獨立執行檔。
*   **NFR-3 安全連線**：SMTP 與 IMAP 預設必須支援 SSL/TLS，並於 Native Image 環境下正確載入作業系統的 Root CA 憑證。
