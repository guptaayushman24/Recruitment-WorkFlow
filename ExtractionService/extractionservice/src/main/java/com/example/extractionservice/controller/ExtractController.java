package com.example.extractionservice.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.extractionservice.dto.ResponseDTO;
import com.example.extractionservice.service.ExtractionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExtractController {
  private final ExtractionService extractionServiceImpl;

  @PostMapping("/extract")
  public ResponseEntity<ResponseDTO> extractInformationFromFile (@ModelAttribute ResumeUploadRequest request) throws IOException{
      String fileType = request.getFile().getContentType();
      if (!fileType.equalsIgnoreCase("application/pdf")){
         return ResponseEntity.ok(ResponseDTO.builder().message("Please upload the pdf file").build());
      }

      // read the bytes now, on the request thread - the MultipartFile's
      // backing storage is not guaranteed to survive past this request
      byte[] resumeBytes = request.getFile().getBytes();

      extractionServiceImpl.extractSkillProjectComponentExpierence(resumeBytes)
              .thenAccept(resumeExtraction -> log.info("Resume extraction finished :::: {}", resumeExtraction))
              .exceptionally(e -> {
                log.error("Exception Occured while processing resume in background :::: {}", e);
                return null;
              });

      return ResponseEntity.accepted()
              .body(ResponseDTO.builder().message("File is uploading, processing in background").build());
  }
}
