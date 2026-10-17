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
  // Round-2 link token (?token= on the coding page); resolves to the candidate's user_id / applied_job_id
  private String token;
}
