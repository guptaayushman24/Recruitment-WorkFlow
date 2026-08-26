package com.example.extractionservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ApplyJobDTO {
  private Integer userId;
  private Integer jobId;
}
