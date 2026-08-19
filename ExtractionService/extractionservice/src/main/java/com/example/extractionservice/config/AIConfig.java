package com.example.extractionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.extractionservice.ai.AIAssistant;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.service.AiServices;

@Configuration
public class AIConfig {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model-name:qwen2.5:7b}")
    private String ollamaModelName;

    @Value("${ollama.embedding-model-name:nomic-embed-text}")
    private String ollamaEmbeddingModelName;

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaEmbeddingModelName)
                .build();
    }

    @Bean
    public AIAssistant aiAssistant(ChatModel chatModel) {
        return AiServices.builder(AIAssistant.class)
                .chatModel(chatModel)
                .build();
    }
}
