# ADR 0001: GraalVM Native Image 與 Jakarta Mail 相容性決策

## 狀態 (Status)
**Proposed** (提出)

## 背景 (Context)
Pollo-Mail CLI 的核心非功能需求之一是「毫秒級的啟動效能」與「免安裝 JRE 的跨平台發布」。為達成此目標，我們採用 **GraalVM Native Image** 將 Spring Boot 應用程式編譯為靜態二進位檔。

然而，本專案依賴 **Jakarta Mail (結合 Angus Mail 實作)** 來處理 SMTP、POP3 與 IMAP 通訊協定。這帶來了嚴峻的技術挑戰：
1. **動態類別載入與反射 (Reflection)**：Jakarta Mail 高度依賴 `ServiceLoader` 與反射機制來動態尋找並載入郵件協定提供者 (Providers) 與 MIME 類型解析器。
2. **資源檔案讀取 (Resources)**：郵件系統在執行期間會頻繁讀取 classpath 下的資源檔，如 `META-INF/javamail.providers`、`META-INF/mailcap` 以及 `javamail.default.address.map` 等。
3. **安全連線 (SSL/TLS)**：SMTP 與 IMAP 預設需要透過 SSL/TLS 進行加密傳輸 (SMTPS/IMAPS)。GraalVM Native Image 預設不包含完整的加密套件與憑證庫。

如果不進行特殊處理，GraalVM 在靜態分析 (Static Analysis) 階段會因為無法預測這些動態行為，而將相關類別與資源剔除，導致編譯出來的執行檔在執行時拋出 `ClassNotFoundException` 或 `NoSuchProviderException`。

## 決策 (Decision)

為確保 Pollo-Mail CLI 能成功編譯為 Native Image 且正常收發郵件，我們做出以下技術決策：

### 1. 利用 Spring AOT 作為基礎
Spring Boot 3.x 內建的 AOT (Ahead-of-Time) 引擎會自動為 Spring 容器內的 Bean 產生大部分的反射與資源設定檔。這將涵蓋我們自訂的 Service 與 Picocli 指令類別。

### 2. 手動提供 Reachability Metadata
由於 Spring AOT 無法自動推斷所有第三方函式庫 (如 Angus Mail) 的內部動態行為，我們必須在專案的 `src/main/resources/META-INF/native-image/` 目錄下，手動提供 GraalVM 所需的 JSON 設定檔：
*   **`resource-config.json`**：強制將 Jakarta Mail 與 Angus Mail 所需的 `META-INF/javamail.*` 與 `META-INF/mailcap` 資源檔打包進二進位檔中。
*   **`reflect-config.json`**：明確宣告 SMTP, SMTPS, IMAP, IMAPS 相關的 Provider 類別 (如 `com.sun.mail.smtp.SMTPTransport`, `com.sun.mail.imap.IMAPStore`) 以及相關的 `DataContentHandler`，允許它們在執行期間被反射實例化。

### 3. 編譯期啟用 HTTPS 與安全加密
在 `build.gradle` 的 GraalVM 配置 (GraalVM Native Image Gradle Plugin) 中，必須明確加上編譯參數：
*   `--enable-https`
*   `--enable-url-protocols=https,http`
確保二進位檔內建 TLS 支援。

### 4. （備選方案拒絕）改用輕量級 HTTP API 客戶端
曾考慮放棄直接操作 SMTP/IMAP，改為呼叫如 SendGrid, Mailgun 等第三方 HTTP API 來發信以避開上述問題。
**拒絕理由**：偏離了 RFP 定義的「原生支援 SMTP/IMAP 伺服器」的產品初衷，這會讓使用者被綁定在特定服務商，無法用於私有地端郵件伺服器。

## 影響 (Consequences)

### 正面影響 (Pros)
*   **達成目標**：能夠成功產出具有毫秒級啟動速度、且能處理標準 IMAP/SMTP 協定的獨立二進位檔。
*   **無 JRE 依賴**：完美達成跨平台原生執行的需求。

### 負面影響與風險 (Cons)
*   **編譯時間長**：GraalVM 編譯 Native Image 原本就相當耗時（數分鐘甚至更久），在 CI/CD 流水線上需要更長的建置時間。
*   **維護成本**：若未來升級 Jakarta Mail 或 Angus Mail 的版本，其內部的類別名稱或資源路徑有變動，我們必須手動更新 `reflect-config.json` 與 `resource-config.json`，否則會導致編譯出的執行檔損壞。這需要透過完整的整合測試（Integration Tests）來把關。
