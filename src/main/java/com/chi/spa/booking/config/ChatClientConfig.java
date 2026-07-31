package com.chi.spa.booking.config;

import com.chi.spa.booking.tool.BookingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    // 記憶容器：記住每個對話最近 20 則訊息（超過會自動把最舊的丟掉）
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    // 方法參數加上 BookingTools bookingTools
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, BookingTools bookingTools) {
        return builder
                .defaultSystem("你是一位專業且禮貌的 SPA 尊榮預約顧問，負責解答顧客關於服務項目、價格與預約流程的問題。")
                // 把記憶功能掛上去，之後每次呼叫都會自動讀取/更新對話歷史
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // 掛上工具，AI 之後可以實際查詢療程、建立預約
                .defaultTools(bookingTools)
                .build();
    }
}