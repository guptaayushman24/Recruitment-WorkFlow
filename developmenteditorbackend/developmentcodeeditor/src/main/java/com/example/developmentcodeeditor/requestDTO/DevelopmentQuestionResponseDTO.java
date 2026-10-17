package com.example.developmentcodeeditor.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevelopmentQuestionResponseDTO {
  private Integer id;
  private String questionName;
  private Integer expectedStatus;
  private String expectedEndpoint;
  private String expectedResponse;
}
