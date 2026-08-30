package com.example.mailservice.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SecureLinkToken {
  private String token;
  private String storedInformation;
  private LocalDateTime expiryDate;
  private String link;
  private String emailAddress;
}
