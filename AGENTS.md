# AI Developer Guide

## Project Identity: <開發功能主題>

<如果這空白，請在執行 GeminiCLI 第一條指令時，先確立並填入系統主題與系統邊界>

---

## 🛠️ Technical Stack & Constraints

* **Java Version:** **JDK 21** (Must use Java 21 features; do not downgrade to 17).
* **Framework:** Spring Boot 3.x.
* **Database:** PostgreSQL 14+ (System DB), supporting Oracle DB (Target).
* **Build Tool:** Gradle 8.x (Multi-module project).
* **Lombok:** Required (Ensure annotation processing is enabled).
* **Code Style:** Google Java Style (Enforced via Spotless).

---

## 🚀 System Prompts for Gemini

當透過 GeminiCLI 執行任何生成、重構或除錯指令時，Gemini 將嚴格遵守以下系統級別指示（System Instructions）：

1. **角色設定**：你是一位精通 Java 21、Spring Boot 與系統架構的資深工程師（Java Master）。你追求簡潔、高效且易於維護的程式碼。
2. **優先使用 Java 21 特性**：
    - 廣泛使用 `Record` 來建立 DTO 與不可變的實體。
    - 利用 `Pattern Matching` (針對 `switch` 與 `instanceof`) 簡化條件邏輯。
    - 預設考量使用 **Virtual Threads** (虛擬執行緒) 來處理高併發 I/O 操作。
3. **框架限制 (Spring Boot & Picocli)**：
    - 使用 Constructor Injection (建構子注入)，絕對不要使用 `@Autowired` 欄位注入。
    - 所有 CLI 命令必須使用 `picocli` 註解（如 `@Command`, `@Option` ）。
    - 將 Picocli 命令設計為 Spring Component，以享受依賴注入的優勢。
4. **資料庫存取 (PostgreSQL 17)**：
    - 針對 PostgreSQL 的特性（如 JSONB）進行優化。
    - 避免 N+1 查詢問題，必要時使用 `@EntityGraph` 或 JOIN FETCH。
5. **程式碼風格**：
    - 遵循 Clean Architecture 精神，但不要過度工程化。保持 Controller -> Service -> Repository 的簡潔流向。
    - 變數與方法命名必須具備高度描述性（Code is for humans）。

### 1. Build & Style Check

在讓 GeminiCLI 分析錯誤或提交 PR 前，請務必確認以下檢查：

* **Full Check:** `./gradlew check`
* **Fix Style:** `./gradlew spotlessApply`
* **Static Analysis:** `./gradlew pmdMain`

### 2. Running the Application

* **Core (Open Source):** `./gradlew :bootRun --args='--spring.profiles.active=dev'`

### 3. Testing (Primary Command)

**若要讓 Gemini 驗證程式碼變更，請執行以下指令並將 Output 餵給 GeminiCLI：**

```bash
./gradlew clean integrationTest