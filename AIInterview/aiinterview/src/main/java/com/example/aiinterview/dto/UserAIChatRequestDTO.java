package com.example.aiinterview.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Getter 
@Setter 
public class UserAIChatRequestDTO {
  private String userId;
  private String userIdAppledJobId;
  private String content;
  private LocalDateTime localDateTime;
}
