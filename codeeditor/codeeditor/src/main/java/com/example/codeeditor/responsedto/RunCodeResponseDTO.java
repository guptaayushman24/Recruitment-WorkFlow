package com.example.codeeditor.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter 
@Setter
@RequiredArgsConstructor 
@AllArgsConstructor 
public class RunCodeResponseDTO {
  private String response;
}
