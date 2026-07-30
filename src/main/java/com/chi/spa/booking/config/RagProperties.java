package com.chi.spa.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spa.rag")
public class RagProperties {

    private int topK = 4;
    private double similarityThreshold = 0.8;
    private String systemPrompt;
}
