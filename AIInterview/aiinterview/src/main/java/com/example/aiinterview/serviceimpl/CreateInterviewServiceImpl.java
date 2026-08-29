package com.example.aiinterview.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.aiinterview.ai.InterviewQuestionAssistant;
import com.example.aiinterview.ai.InterviewQuestions;
import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO;
import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO.JobDescription;
import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO.Resume;
import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO.UserDetail;
import com.example.aiinterview.dto.InterviewQuestionsRecord;
import com.example.aiinterview.service.CreateInterviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateInterviewServiceImpl implements CreateInterviewService {

  private final InterviewQuestionAssistant interviewQuestionAssistant;

  @Override
  public InterviewQuestionsRecord createInterviewQuestions(ExtractionResumeJobDescriptionDTO extractionResumeJobDescriptionDTO) {
    Resume resume = extractionResumeJobDescriptionDTO.getResumeExtraction();
    JobDescription jobDescription = extractionResumeJobDescriptionDTO.getJobDescriptionExtraction();
    UserDetail userDetail = extractionResumeJobDescriptionDTO.getUserDetail();

    InterviewQuestions interviewQuestions = interviewQuestionAssistant.generateQuestions(
        emptyIfNull(resume.getSkills()),
        emptyIfNull(resume.getProjectComponents()),
        emptyIfNull(resume.getExperience()),
        emptyIfNull(jobDescription.getJobSkills()),
        emptyIfNull(jobDescription.getJobExperience()),
        emptyIfNull(jobDescription.getJobComponents()));

    return InterviewQuestionsRecord.builder()
        .userId(userDetail.getUserId())
        .appliedJobId(userDetail.getAppliedJobId())
        .questions(interviewQuestions.getQuestions())
        .build();
  }
  private static List<String> emptyIfNull(List<String> values) {
    return values == null ? List.of() : values;
  }
}
