package com.chi.spa.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagResponse {
    /**
     * AI 生成的回應內容
     */
    private String answer;
}
