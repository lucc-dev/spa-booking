package com.chi.spa.booking.service;

import com.chi.spa.booking.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RagProperties ragProperties;

    private static final String NO_MATCH_RESPONSE =
            "很抱歉，目前無法找到與您問題相關的資訊，建議您直接致電門市洽詢，我們會盡快為您服務！";

    public void importKnowledgeBase(List<String> contents) {
        List<Document> documents = contents.stream()
                .map(content -> new Document(content, Map.of("source", "spa_knowledge_base")))
                .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("成功匯入 {} 筆知識庫資料至 Elasticsearch", documents.size());
    }

    public String askAi(String userMessage, String conversationId) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userMessage)
                .topK(ragProperties.getTopK())
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        log.info("檢索到的相關內容片段數: {}", similarDocuments.size());

        // 查不到相關資料時，不直接擋掉回覆制式訊息，而是把「查無資料」這件事
        // 一併告訴 AI，讓 AI 依情境自行判斷：
        // 若是知識性問題，就誠實告知查無相關資訊；
        // 若是預約、查詢、修改、取消等訂位需求，則依 system prompt 的流程直接呼叫對應工具。
        String context = similarDocuments.isEmpty()
                ? "（知識庫中查無相關資料。若顧客詢問的是一般知識性問題，請誠實告知無法查到相關資訊，建議致電門市洽詢；若顧客是要預約、查詢空位、修改或取消預約，請忽略此提示，直接依照下方訂位流程呼叫對應工具處理。）"
                : similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        String fullPrompt = """
                相關背景知識資訊如下：
                %s
                
                顧客提出的問題：
                %s
                """.formatted(context, userMessage);

        return chatClient.prompt()
                .system(ragProperties.getSystemPrompt())
                .user(fullPrompt)
                // 帶上對話 ID，MessageChatMemoryAdvisor 會依此讀取/更新該對話的歷史紀錄
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}