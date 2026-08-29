package com.example.aiinterview.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ExtractionResumeJobDescriptionDTO {
  private Resume resumeExtraction;
  private JobDescription jobDescriptionExtraction;
  private UserDetail userDetail;

  @Data
  @Getter
  @Setter
  public static class Resume {
    private List<String> skills;
    private List<String> experience;
    private List<String> projectComponents;
  }

  @Data
  @Getter
  @Setter
  public static class JobDescription {
    private List<String> jobSkills;
    private List<String> jobExperience;
    private List<String> jobComponents;
  }

  @Data
  @Getter
  @Setter
  public static class UserDetail{
    private Integer userId;
    private Integer appliedJobId;
  }
}
