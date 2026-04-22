# 專案 Agent 協作開發流程 (Agentic SDLC)

本專案採用 AI Agent 協作開發模式，分為 5 個階段。每個階段由專屬的 Agent 負責，並透過 Markdown 文件進行狀態交接。

## 流程概覽

1. **需求訪談 (PM Agent)** ：釐清模糊想法。
2. **需求與架構確認 (Architect Agent)** ：產出 `PRD` 與 `ADR` 。
3. **程式開發 (Developer Agent)** ：產出 `PLAN.md` 並實作程式碼。
4. **單元測試 (Unit Test Agent)** ：確保方法級別的正確性。
5. **完整功能測試 (QA Agent)** ：確保業務邏輯符合 PRD 驗收標準。

## 文件範本 (Templates)

### 1 PRD 範本 (存放於 docs/prd/YYYYMMDD-feature-name.md)

```markdown
# [功能名稱] 產品需求文件

## 1. 背景與目標
- **為什麼要做？** - **預期目標？**

## 2. 使用者故事 (User Stories)
- 作為 [角色]，我想要 [功能]，以便於 [商業價值]。

## 3. 驗收標準 (Acceptance Criteria - AC)
- [ ] 條件 1：當 [情境] 發生時，系統應該 [結果]。
- [ ] 條件 2：...
```

### 2 ADR 範本 (存放於 docs/adr/YYYYMMDD-decision-name.md)

```markdown
# ADR: [技術決策名稱]

## 1. 背景 (Context)
- 描述促使此決策的技術背景或 PRD 需求。

## 2. 決策 (Decision)
- 我們決定採用 [技術/架構/套件]。

## 3. 考量與後果 (Consequences)
- **優點**: 
- **缺點/妥協**:
```