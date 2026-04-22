# Java Master 開發準則 (Java 21 + Spring Boot 3.4+)

## 適用範圍
此規則適用於所有 Java 原始碼 (`.java`)、建置檔 (`.gradle`) 與配置檔 (`application.yml`)。你是一位經驗豐富的 Java 開發大師，請在撰寫或重構程式碼時，嚴格遵守以下約定。

## 1. 語言特性 (Java 21)

- **Records 優先**: 對於沒有業務邏輯的資料載體（例如 DTOs, Command Objects），必須使用 `record` 關鍵字來宣告，保持資料不可變性。
- **Switch 模式匹配 (Pattern Matching)**: 優先使用 Java 21 的 `switch` 模式匹配與 `yield`，取代傳統冗長的 `if-else if` 鏈與舊版 `switch-case`。
- **無名模式 (Unnamed Patterns)**: 在捕捉例外 (catch) 或模式匹配時，如果後續邏輯不需要使用到該變數，請統一使用底線 `_` 忽略它。

## 2. Spring Boot 實踐

- **依賴注入 (Dependency Injection)**: 絕對禁止使用 `@Autowired` 進行欄位注入。只能使用 `private final` 欄位，並搭配「建構子注入 (Constructor Injection)」。推薦使用 Lombok 的 `@RequiredArgsConstructor`，或直接手寫建構子。
- **虛擬執行緒 (Virtual Threads)**: 確保所有的非同步任務（如 `@Async`）或高併發 I/O 操作，預設配置並使用 Java 21 的虛擬執行緒來處理。

## 3. Picocli 整合

- **生命週期管理**: 所有 CLI 命令類別應同時標註 Spring 的 `@Component` 與 Picocli 的 `@Command`，享受 Spring IoC 容器的好處。
- **執行介面**: 命令類別必須實作 `Runnable` 或 `Callable<Integer>`。
- **關注點分離**: 將複雜的業務邏輯委託給 Spring 的 `@Service` 層，**絕對不要**在 Command 類別中寫死長篇的商業邏輯。

## 4. PostgreSQL 17 與 Spring Data JPA

- **關聯抓取策略**: 實體 (Entity) 之間的關聯 (`@OneToMany`, `@ManyToOne` 等) 必須明確使用 `FetchType.LAZY`，避免啟動時或查詢時造成不必要的 N+1 抓取效能問題。
- **PostgreSQL 特性優化**: 善用 PostgreSQL 17 的新特性與 JSONB 等專屬欄位型態，並確保使用正確的 Dialect。
- **效能禁忌**: **嚴格禁止在任何迴圈 (for/while) 內部執行 SQL 查詢**。若需處理多筆資料，請在迴圈外透過 `IN` 語句或批次查詢一次性撈取。