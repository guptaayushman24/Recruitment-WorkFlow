package com.example.mailservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Getter 
@Setter
public class UserInterviewReport {
  private Integer userId;
  private Integer appliedJobId;
  private String email;
  private String jobTitle;
  private Double userScore;
}
