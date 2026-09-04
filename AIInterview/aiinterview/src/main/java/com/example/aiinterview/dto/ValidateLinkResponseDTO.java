package com.example.aiinterview.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Setter
@Getter
public class ValidateLinkResponseDTO {
  private String status;
  private LocalDate expiryDate;
}
