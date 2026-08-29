package com.example.aiinterview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.aiinterview.ai.InterviewQuestionAssistant;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

@Configuration
public class AIConfig {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model-name:qwen2.5:7b}")
    private String ollamaModelName;

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .build();
    }

    @Bean
    public InterviewQuestionAssistant interviewQuestionAssistant(ChatModel chatModel) {
        return AiServices.builder(InterviewQuestionAssistant.class)
                .chatModel(chatModel)
                .build();
    }
}
