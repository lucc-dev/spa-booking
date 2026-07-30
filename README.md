# 美容 SPA 預約系統
本專案為一套現代化的美容 SPA 服務預約系統，基於 Java 21 與 Spring Boot 3 開發。
系統採用分層架構設計，結合 Spring Data JPA 處理 MySQL 關聯式資料庫，並整合 Redis 作為快取機制以提升熱門預約時段與服務項目的查詢效能。
專案同時支援 Docker Compose 容器化部署與 H2 Database 單元測試，具備高擴充性與易維護性。

1. Model (Data Entities)
負責對應 MySQL 資料庫表單結構。
定義預約（Booking）、顧客（Customer）、SPA 服務項目（ServiceItem）等核心資料實體與關聯關係。

2. DTO (Data Transfer Objects)
隔離內部 Entity 與外部 API 請求介面，避免直接暴露資料庫結構並確保傳入資料格式安全。
包含客戶端送出的預約請求與資料傳輸物件（如 BookingRequestDTO）。

3. Repository
繼承 JpaRepository，提供 CRUD 與客製化 JPQL 查詢。
負責與 MySQL 資料庫互動，高效執行預約紀錄的檢索與持久化。

4. Service
系統核心業務邏輯層（如：預約時段衝突檢查、預約狀態變更）。
整合 Spring Cache (Redis)，針對高頻讀取的服務清單與熱門時段進行快取優化。

5. Controller
提供標準 RESTful API 介面，處理 HTTP 請求（GET, POST, PUT, DELETE）。
負責接收 DTO、驗證參數，並呼叫 Service 層處理後回傳 JSON 結果。

6. Exception (Custom Exception)
建立自訂的 BusinessException（繼承自 RuntimeException），專門用於拋出系統中的商業邏輯錯誤與異常狀態。
透過傳入自訂錯誤訊息（message），確保系統在遇到業務邏輯阻礙時能明確告知原因。
