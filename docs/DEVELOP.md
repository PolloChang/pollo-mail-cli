# 如何開發 (How to Build)

本文件將介紹 Pollo-Mail CLI 的技術架構、開發環境設定，以及如何透過 GraalVM 編譯成 Native Image 執行檔。

## 🏗️ 系統架構 (Architecture)

本專案採用 **Java 21** 搭配 **Spring Boot 3** 建構，並使用 **Picocli** 處理終端機命令列解析。
專案結構遵循簡化的 Clean Architecture 概念，主要分為以下幾個核心套件 (`com.pollo.mail.*`)：

*   **`command` (控制器層)**
    *   負責解析終端機輸入的參數。
    *   包含：`PolloMailCommand` (主進入點)、`SetupCommand`、`SendCommand`、`FetchCommand`。
*   **`service` (業務邏輯層)**
    *   負責與外部系統 (IMAP/SMTP) 互動與資料處理。
    *   包含：`MailService` (處理發信與 Jakarta Mail Session)、`FetchService` (處理收信與 Markdown 轉譯)。
*   **`config` (配置層)**
    *   透過 Java Records 映射 `~/.config/pollo-mail/config.yaml`。
*   **`util` (工具層)**
    *   `CryptoUtil`: 實作綁定本機硬體 (MAC Address + OS) 的 AES-256 PBKDF2 加密與解密邏輯。

> **架構決策紀錄 (ADR)**: 關於為何選擇 GraalVM 搭配 Jakarta Mail，以及我們如何解決反射 (Reflection) 問題，請參閱 [docs/adr/0001-graalvm-jakarta-mail.md](adr/0001-graalvm-jakarta-mail.md)。

---

## 🛠️ 開發環境設定

### 1. 安裝 Java 21 與 Gradle
強烈建議直接安裝 **GraalVM JDK 21**，以便在本地直接測試 AOT 編譯。
您可以使用 SDKMAN 或從 [Oracle 官方網站](https://www.oracle.com/java/technologies/downloads/#graalvmjava21) 下載。

確保您的環境變數指向正確的路徑：
```bash
export JAVA_HOME=/usr/local/lib/graalvm/graalvm-jdk-21
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. 本地執行與除錯
在開發過程中，您不需要每次都編譯為 Native Image。您可以直接使用 Gradle 的 `bootRun` Task 進行測試：

```bash
./gradlew bootRun --args="send --help"
```

> **注意 (Gradle 終端機輸入問題)**：
> 由於 `./gradlew bootRun` 執行時沒有真實的 TTY 終端機，`System.console()` 將回傳 `null`。
> 為了方便測試，我們的程式碼 (`SetupCommand.java`) 已實作了 `Scanner` 降級機制。但在開發環境下，您輸入的密碼將會是**明文可見**的。此行為在編譯為最終二進位檔後將恢復為隱藏輸入。

---

## 📦 GraalVM Native Image 打包指南

當您開發完成並準備發布時，可以透過 Spring Boot 3 內建的 AOT (Ahead-of-Time) 引擎將其編譯為極速的原生執行檔。

### 1. 執行編譯
請確保您的 `JAVA_HOME` 指向 GraalVM，然後執行以下指令：

```bash
./gradlew nativeCompile
```
此過程將會耗費大量 CPU 資源與記憶體，並耗時數分鐘（取決於硬體效能）。

### 2. 關於 Jakarta Mail 與 HTTPS/TLS 問題
Jakarta Mail 在建立 SSL/TLS 連線時高度依賴動態反射 (Reflection) 與 JCE 安全提供者。
若您在打包過程中或執行 Native Image 時遇到 `ClassNotFoundException` 或 SSL 連線錯誤，請確認以下事項：

1.  **`build.gradle` 的設定**：
    確保 `graalvmNative` 區塊有啟用 HTTPS 支援：
    ```groovy
    graalvmNative {
        binaries {
            main {
                buildArgs.add('--enable-https')
            }
        }
    }
    ```
2.  **Reachability Metadata (動態反射中繼資料)**：
    如果您引入了新的動態載入套件，您需要先利用 GraalVM Tracing Agent 收集 metadata，並將其放入 `src/main/resources/META-INF/native-image/` 目錄中：
    ```bash
    ./gradlew bootJar
    java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar build/libs/pollo-mail-cli-0.0.1-SNAPSHOT.jar [您的指令]
    ```

編譯成功後，產生的執行檔會位於 `build/native/nativeCompile/pollo-mail-cli`，您可以將其移動到系統路徑並發布給使用者。
