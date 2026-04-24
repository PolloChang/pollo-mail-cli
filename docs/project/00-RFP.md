# 專案需求建議書 (RFP)：跨平台原生命令列郵件工具 (Project Pollo-Mail CLI)

## 1. 專案概述 (Project Overview)
本專案旨在開發一款高效、輕量且支援跨平台的命令列郵件收發工具（CLI）。針對現代自動化維運、CI/CD 流程以及開發者日常終端機作業需求，本工具將結合 Java 豐富的開源生態與 GraalVM 的原生編譯技術，解決傳統 Java 程式啟動延遲的問題，提供毫秒級啟動的「即點即用」體驗。

## 2. 核心技術堆疊 (Technical Stack)
專案將嚴格採用現代化 Java 開發標準：
* **核心語言**：Java 21（全面採用 Records、Pattern Matching 等新特性）。
* **應用框架**：Spring Boot 3.4+ 搭配 `picocli-spring-boot-starter`，利用 Spring IoC 容器管理命令列元件的生命週期。
* **CLI 框架**：Picocli（支援自動補完、ANSI 色彩輸出與多層級子命令）。
* **郵件協議**：Jakarta Mail (配合 Angus Mail 實作)，處理 SMTP、POP3 與 IMAP 協議。
* **配置解析**：SnakeYAML。
* **編譯與發布**：Gradle 8.x 結合 GraalVM Native Image，並透過 GitHub Actions 建立自動化 CI/CD 流水線，輸出靜態二進位檔。

## 3. 功能需求 (Functional Requirements)

### 3.1 配置管理模組
* 系統需支援從使用者目錄讀取設定檔（例如 `~/.config/pollo-mail/config.yaml`）。
* 設定檔需包含 SMTP/IMAP 伺服器位址、通訊埠與帳號資訊。
* 具備高安全性考量：密碼或敏感憑證必須支援從環境變數或整合系統級密碼管理工具（如 `pass` Password Store）動態讀取，嚴禁明碼存放於設定檔。

### 3.2 核心指令模組
需實作直觀且易於自動化腳本調用的子命令結構：
* **發送郵件 (`send`)**：
  * 支援必填參數：收件人 (`-t`, `--to`)。
  * 支援選填參數：主旨 (`-s`, `--subject`)、內文 (`-b`, `--body`)。
  * 支援單一或多個附件夾帶 (`-a`, `--attach`)。
  * 支援 HTML 格式解析與發送。
* **接收/檢視郵件 (`fetch`)**：
  * 支援讀取最新郵件標頭與內文。
  * 支援數量限制參數 (`--limit`) 以控制拉取範圍。

## 4. 非功能與系統需求 (Non-Functional Requirements)
* **跨平台支援**：必須提供 Linux、Windows (.exe) 與 macOS (包含 Intel 與 Apple Silicon M 系列) 的原生執行檔，使用者無需在本地端安裝 JRE 即可執行。
* **啟動效能**：透過 GraalVM 編譯，執行啟動時間必須控制在毫秒級別，以符合 CLI 工具的操作直覺與自動化排程的嚴苛要求。
* **網路與安全**：需妥善處理 Native Image 中的 SSL/TLS 憑證與反射（Reflection）問題，確保加密連線（SMTPS/IMAPS）穩定運作。

## 5. 開發與驗收流程 (Development & Acceptance Process)
本專案將遵循 Agentic SDLC 協作開發流程：
1. **需求對焦**：依據此 RFP 轉化為正式的產品需求文件（PRD），包含詳細的 User Stories 與驗收標準 (AC)。
2. **架構決策**：確立 GraalVM Native Image 處理 Jakarta Mail 依賴的架構決策紀錄（ADR）。
3. **單元測試**：針對配置解析與郵件格式封裝邏輯，必須具備完整的 JUnit 5 + Mockito 測試覆蓋。
4. **整合驗證**：透過 `@SpringBootTest` 與 Testcontainers 驗證完整的依賴注入與端到端執行路徑。最終交付需包含 GitHub Actions 成功編譯各平台執行檔的通過紀錄。
