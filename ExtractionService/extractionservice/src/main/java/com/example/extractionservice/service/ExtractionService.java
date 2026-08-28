package com.example.extractionservice.service;

import java.util.concurrent.CompletableFuture;

import com.example.extractionservice.ai.ResumeExtraction;
import com.example.extractionservice.dto.ApplyJobDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface ExtractionService {
   public CompletableFuture<ResumeExtraction> extractSkillProjectComponentExpierence (byte[] resumeBytes,String userEmail);
   public CompletableFuture<ResumeExtraction> extractSkillProjectComponentExpierenceFromJobDescription (String jobDescription);

   public void applyJob (ApplyJobDTO applyJobDTO);

   public void findMatchInUserResumeAndJobDescription () throws JsonProcessingException;
}
