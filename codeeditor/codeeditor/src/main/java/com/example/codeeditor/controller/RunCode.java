package com.example.codeeditor.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.codeeditor.requestdto.RunCodeRequestDTO;
import com.example.codeeditor.responsedto.RunCodeResponseDTO;
import com.example.codeeditor.serviceimpl.ValidateRunCodeServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequiredArgsConstructor 
public class RunCode {
  private final ValidateRunCodeServiceImpl validateRunCodeServiceImpl;
  @PostMapping("/runcode")
  public ResponseEntity<RunCodeResponseDTO> postMethodName(@RequestBody RunCodeRequestDTO runCodeRequestDTO) throws Exception {
    RunCodeResponseDTO runCodeResponseDTO =   validateRunCodeServiceImpl.validateRunCodeRequest(runCodeRequestDTO);
      return ResponseEntity.status(200)
       .body(runCodeResponseDTO);
  }
  // Apply validation on the code language  written by the user
}
