package com.example.aiinterview.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Getter 
@Setter 
public class UserAIChatRequestDTO {
  private Integer userId;
  private Integer userIdAppledJobId;
  private String content;
  private String question;
  private String jobDescription;
  private LocalDateTime localDateTime;
}
