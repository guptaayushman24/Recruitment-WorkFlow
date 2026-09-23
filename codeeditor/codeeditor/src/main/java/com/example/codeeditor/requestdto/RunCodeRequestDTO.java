package com.example.codeeditor.requestdto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Getter 
@Setter 
public class RunCodeRequestDTO {
  private String programmingLanguage;
  private String sourceCode;
  //private String programmingInput;
  private Object programmingInput;
  private Integer id;
}
