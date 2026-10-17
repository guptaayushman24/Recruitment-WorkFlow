package com.example.codeeditor.requestdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodingQuestionStatusDTO {
  @Builder.Default
  private Integer totalTestCase = 0;

  @Builder.Default
  private Integer passedTestCase = 0;

  @Builder.Default
  private Integer failedTestCase = 0;
}
