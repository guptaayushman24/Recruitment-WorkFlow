package com.example.aiinterview.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class InterviewQuestionsRecord {
  private Integer userId;
  private Integer appliedJobId;
  private List<String> questions;
}
