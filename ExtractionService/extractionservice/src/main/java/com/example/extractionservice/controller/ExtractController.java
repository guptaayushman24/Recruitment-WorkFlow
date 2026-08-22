package com.example.extractionservice.controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.extractionservice.dto.ResponseDTO;
import com.example.extractionservice.model.UserData;
import com.example.extractionservice.repository.SaveUserDetail;
import com.example.extractionservice.service.ExtractionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExtractController {
  private final ExtractionService extractionServiceImpl;
  private final SaveUserDetail saveUserDetail;
  private  final String UPLOAD_DIR = "/Users/ayushmangupta/Documents/Recruitment_WorkFlow/file";
  @PostMapping("/extract")
  public ResponseEntity<ResponseDTO> extractInformationFromFile (@ModelAttribute ResumeUploadRequest request) throws IOException{
    if (request.getFile().isEmpty()){
       return ResponseEntity.badRequest()
              .body(ResponseDTO.builder().message("Please select file to upload").build());
    }
    Path uploadPath = Paths.get(UPLOAD_DIR);
    String fileName = request.getEmail()+" "+request.getFile().getOriginalFilename();
    Path filePath = uploadPath.resolve(fileName);
    Files.copy(request.getFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      String fileType = request.getFile().getContentType();
      if (!fileType.equalsIgnoreCase("application/pdf")){
         return ResponseEntity.ok(ResponseDTO.builder().message("Please upload the pdf file").build());
      }

      // read the bytes now, on the request thread - the MultipartFile's
      // backing storage is not guaranteed to survive past this request
      byte[] resumeBytes = request.getFile().getBytes();

      extractionServiceImpl.extractSkillProjectComponentExpierence(resumeBytes,request.getEmail())
              .thenAccept(resumeExtraction -> log.info("Resume extraction finished :::: {}", resumeExtraction))
              .exceptionally(e -> {
                log.error("Exception Occured while processing resume in background :::: {}", e);
                return null;
              });
      
      if (filePath!=null){
        // Save the userDetail and the filePath in db
        UserData userData = new UserData();
        userData.setFirstName(request.getFirstName());
        userData.setLastName(request.getLastName());
        userData.setEmail(request.getEmail());
        userData.setResumePath(filePath.toString());

        saveUserDetail.saveUserDetail(userData);
      }
      return ResponseEntity.accepted()
              .body(ResponseDTO.builder().message("File is uploading, processing in background").build());
  }
}
