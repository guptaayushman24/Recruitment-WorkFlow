package com.example.interviewevaluation.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// Same shape as mailservice's SecureLinkToken - this is the payload of the user-interview-activation topic
@Data
@Getter
@Setter
public class SecureLinkToken {
  private String token;
  private String storedInformation;
  private LocalDateTime expiryDate;
  private String link;
  private String emailAddress;
  private Integer round;
}
