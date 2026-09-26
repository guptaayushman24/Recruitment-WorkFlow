package com.example.codeeditor.responsedto;

import java.util.List;

import com.example.codeeditor.requestdto.SampleCodingTestCaseDTO;

import lombok.Data;

@Data
public class CodingQuestion {
  private Long id;
  private String title;
  private String codingQuestion;
  private List<SampleCodingTestCaseDTO> testCases;
  private String questionCodingTemplate;
  private String userCodingTemplate;
}
