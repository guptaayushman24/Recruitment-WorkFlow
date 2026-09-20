package com.example.codeeditor.service;

import com.example.codeeditor.requestdto.RunCodeRequestDTO;
import com.example.codeeditor.responsedto.RunCodeResponseDTO;

public interface ValidateRunCode {
  public RunCodeResponseDTO validateRunCodeRequest (RunCodeRequestDTO runCodeRequestDTO) throws Exception;
}
