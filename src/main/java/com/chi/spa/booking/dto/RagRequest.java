package com.chi.spa.booking.dto;

import lombok.Data;

@Data
public class RagRequest {
    /**
     * 使用者提問或諮詢內容
     */
    private String message;

    /**
     * 客戶 ID（可選）
     */
    private Long customerId;
}
