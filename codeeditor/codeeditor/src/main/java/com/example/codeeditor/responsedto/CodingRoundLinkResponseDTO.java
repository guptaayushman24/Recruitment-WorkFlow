package com.example.codeeditor.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingRoundLinkResponseDTO {
  private Boolean valid;
  private String message;
}
