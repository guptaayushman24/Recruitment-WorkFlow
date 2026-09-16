package com.example.interviewevaluation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.interviewevaluation.ai.InterviewEvaluationAssistant;

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
    public InterviewEvaluationAssistant evaluateUserResponseOfInterview(ChatModel chatModel) {
        return AiServices.builder(InterviewEvaluationAssistant.class)
                .chatModel(chatModel)
                .build();
    }
}
