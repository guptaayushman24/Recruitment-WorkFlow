package com.example.extractionservice.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ResumeJobMatchDTO {
  private Long embeddingId;
  private Integer userId;
  private Integer jobId;
  private Integer appliedJobs;
  private float[] embedding;
  private List<String> skills;
  private List<String> experience;
  private List<String> projectComponents;
  private List<String> jobSkills;
  private List<String> jobExperience;
  private List<String> jobComponents;
  private float[] jobDescriptionEmbedding;
}
