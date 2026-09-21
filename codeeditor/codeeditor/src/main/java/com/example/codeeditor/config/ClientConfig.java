package com.example.codeeditor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.example.codeeditor.service.CodingInterviewService;

@Configuration 
public class ClientConfig {
  @Bean
  public CodingInterviewService condingInterviewService(){
    RestClient restClient = RestClient.builder().baseUrl("https://vercel.app").build();
    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

    return factory.createClient(CodingInterviewService.class);
  }
}

// fetchCodingQuestions
