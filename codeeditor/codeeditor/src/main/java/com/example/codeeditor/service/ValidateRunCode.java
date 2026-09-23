package com.example.codeeditor.service;

import com.example.codeeditor.requestdto.RunCodeRequestDTO;
import com.example.codeeditor.requestdto.SampleCodingTestCaseDTO;

public interface ValidateRunCode {
  public SampleCodingTestCaseDTO validateRunCodeRequest (RunCodeRequestDTO runCodeRequestDTO) throws Exception;
}
