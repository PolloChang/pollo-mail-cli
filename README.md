# Java Master AI Skill for Google Antigravity

這是一個專為現代化 Java 開發設計的 AI Skill 集合，特別針對 **Google Antigravity** 這套「代理人優先 (Agent-first)」的 IDE 進行了最佳化。本專案定義了 Antigravity Agent 在規劃、編寫與驗證 Java 應用程式時應遵循的系統架構與最佳實踐。

## 技術選型

- **語言**: Java 21
- **核心框架**: Spring Boot 3.4+ (對標現代化與未來的 Spring Boot 4 生態)
- **資料庫**: PostgreSQL 17 (搭配 Spring Data JPA)
- **命令列框架**: `picocli-spring-boot-starter`
- **建置工具**: Gradle

## 目錄結構

- `.antigravity/rules/`: 包含 Google Antigravity 專用的工作區規則 (Workspace Rules)，用於規範 Agent 的程式碼生成與內部思考邏輯。
- `docs/` 或 `.skills/`: 存放具體的 AI Skill 定義檔與 Artifacts 參考。
- `ANTIGRAVITY.md`: 專門針對 Antigravity Agent 的角色設定與系統級指令。
- `EXAMPLES.md`: 核心技術棧的標準程式碼範例，供 Agent 在建立 Implementation Plan 時參考。

## 如何使用

1. 將此結構複製到您的專案根目錄。
2. 使用 **Google Antigravity** 開啟此專案作為工作區 (Workspace)。
3. 在 Antigravity 右下角的「Settings (設定)」中，將 `.antigravity/rules/` 內的規則匯入或設定為 **Workspace Rules** (工作區規則)，確保 Agent 能嚴格遵守 Java 21 與 Spring Boot 的規範。
4. 透過 Antigravity 的 **Agent Manager** 建立新的 Agent 並下達開發指令，Agent 將會自動參考 `EXAMPLES.md` 產生結構化的 Task Lists 與實作計畫。


```bash
java-ai-skills-manager/
├── docs/                         (或 skills/ 存放其他技能說明)
├── src/                          (您的 Java 原始碼存放區)
├── ANTIGRAVITY.md                (Agent 系統提示詞與角色設定)
├── EXAMPLES.md                   (標準寫法範例)
├── README.md                     (專案首頁，包含如何設定的說明)
└── build.gradle                  (建置檔)

```

## Referance

* [Spring AI Agentic Patterns (Part 1): Agent Skills - Modular, Reusable Capabilities](https://spring.io/blog/2026/01/13/spring-ai-generic-agent-skills)