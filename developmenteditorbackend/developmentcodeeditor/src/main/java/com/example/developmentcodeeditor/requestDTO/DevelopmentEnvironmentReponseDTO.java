package com.example.developmentcodeeditor.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class DevelopmentEnvironmentReponseDTO {
  private String fileName;
  private String fileType;
  private String fileContent;
  private String pathDirectory;
}
