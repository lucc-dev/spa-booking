package com.chi.spa.booking.controller;

import com.chi.spa.booking.dto.RagRequest;
import com.chi.spa.booking.dto.RagResponse;
import com.chi.spa.booking.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * 1. SPA AI 諮詢與問答 API
     */
    @PostMapping("/chat")
    public ResponseEntity<RagResponse> chat(@RequestBody RagRequest request) {
        // 用 customerId 當作對話 ID；沒帶 customerId 就歸類成同一組訪客對話
        String conversationId = request.getCustomerId() != null
                ? String.valueOf(request.getCustomerId())
                : "guest";

        String answer = ragService.askAi(request.getMessage(), conversationId);
        return ResponseEntity.ok(RagResponse.builder()
                .answer(answer)
                .build());
    }

    /**
     * 2. 初始化／補充 SPA 知識庫內容至 Elasticsearch API
     */
    @PostMapping("/knowledge")
    public ResponseEntity<String> importKnowledge(@RequestBody List<String> knowledgeList) {
        ragService.importKnowledgeBase(knowledgeList);
        return ResponseEntity.ok("SPA 知識庫已成功寫入 Elasticsearch！");
    }
}
