package com.example.extractionservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EmailDTO {
  private String userFirstName;
  private String userLastName;
  private String userEmailAddress;
}
