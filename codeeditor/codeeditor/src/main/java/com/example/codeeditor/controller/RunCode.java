package com.example.codeeditor.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.example.codeeditor.requestdto.RunCodeRequestDTO;
import com.example.codeeditor.responsedto.CodingQuestion;
import com.example.codeeditor.responsedto.RunCodeResponseDTO;
import com.example.codeeditor.serviceimpl.StartCodingRoundServiceImpl;
import com.example.codeeditor.serviceimpl.ValidateRunCodeServiceImpl;

import lombok.RequiredArgsConstructor;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequiredArgsConstructor
public class RunCode {
  private final ValidateRunCodeServiceImpl validateRunCodeServiceImpl;
  private final StartCodingRoundServiceImpl startCodingRoundServiceImpl;

  @PostMapping("/runcode")
  public ResponseEntity<RunCodeResponseDTO> postMethodName(@RequestBody RunCodeRequestDTO runCodeRequestDTO) throws Exception {
    RunCodeResponseDTO runCodeResponseDTO =   validateRunCodeServiceImpl.validateRunCodeRequest(runCodeRequestDTO);
      return ResponseEntity.status(200)
       .body(runCodeResponseDTO);
  }

  @GetMapping(value = "/startCodingRound")
  public ResponseEntity<List<CodingQuestion>> getMethodName() {
    List<CodingQuestion> codingQuestions = startCodingRoundServiceImpl.startCodingRound();
    return ResponseEntity.status(200)
     .body(codingQuestions);
  }

  // Apply validation on the code language  written by the user
}
