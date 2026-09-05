package com.example.aiinterview.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Getter 
@Setter
public class StartInterviewResponseDTO {
  private Integer status;
  private LocalDateTime expiryDate;
}
