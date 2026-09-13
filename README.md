# 美容 SPA 預約系統 (含 AI 智能顧問)

本專案為一套現代化的美容 SPA 服務預約系統，基於 **Java 21** 與 **Spring Boot 3** 開發。
系統採用分層架構設計，整合 **Spring Data JPA** (MySQL) 與 **Redis** 快取機制，並導入 **Spring AI (Google Gemini)** 結合 **Elasticsearch 向量資料庫**，實現支援語意搜尋 (RAG) 與自動化預約 (Function Calling) 的 AI 智能對話顧問。
專案同時支援 Docker Compose 容器化部署與 H2 Database 單元測試，具備高擴充性與易維護性。

---

## 架構與核心模組

### 1. Model (Data Entities)
* 負責對應 MySQL 資料庫表單結構。
* 定義預約（Booking）、顧客（Customer）、SPA 服務項目（ServiceItem）等核心資料實體與關聯關係。

### 2. DTO (Data Transfer Objects)
* 隔離內部 Entity 與外部 API 請求介面，避免直接暴露資料庫結構並確保傳入資料格式安全。
* 包含客戶端送出的預約請求與資料傳輸物件（如 BookingRequestDTO）。

### 3. Repository
* 繼承 `JpaRepository`，提供 CRUD 與客製化 JPQL 查詢。
* 負責與 MySQL 資料庫互動，高效執行預約紀錄的檢索與持久化。

### 4. Service (業務邏輯與 AI 模組)
* **核心業務邏輯**：負責預約時段衝突檢查、預約狀態變更等，並整合 **Spring Cache (Redis)** 優化高頻讀取的服務項目與熱門時段。
* **智能 AI 顧問與 Function Calling 整合**：採用 Spring AI + Google Gemini，透過 Function Calling (Tool Calling) 機制綁定後端業務服務，使 AI 顧問能理解使用者口語意圖，自動執行「即時空位查詢、建立預約、變更與取消預約」等操作。
* **RAG 知識庫與 Elasticsearch 向量檢索**：構建 RAG (Retrieval-Augmented Generation) 架構，使用 **Elasticsearch Vector Store** 與 Gemini Embedding 實現 SPA 療程與門市規範的語意化向量檢索。搭配 `MessageWindowChatMemory` 維護多輪對話記憶，有效消除 AI 幻覺，提升客服回答精準度。

### 5. Controller
* 提供標準 RESTful API 介面，處理 HTTP 請求（GET, POST, PUT, DELETE）。
* 包含傳統預約介面與 AI 對話互動入口，負責接收 DTO、驗證參數，並呼叫 Service 層處理後回傳 JSON 結果。

### 6. Exception (Custom Exception)
* 建立自訂的 `BusinessException`（繼承自 `RuntimeException`），專門用於拋出系統中的商業邏輯錯誤與異常狀態。
* 透過傳入自訂錯誤訊息（message），確保系統在遇到業務邏輯阻礙時能明確告知原因。
