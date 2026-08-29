package com.example.aiinterview.service;

import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO;
import com.example.aiinterview.dto.InterviewQuestionsRecord;

public interface CreateInterviewService {
  InterviewQuestionsRecord createInterviewQuestions(ExtractionResumeJobDescriptionDTO extractionResumeJobDescriptionDTO);
}
