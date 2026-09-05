package com.example.aiinterview.service;

import java.security.Principal;
import java.util.List;

import com.example.aiinterview.dto.UserAIChatRequestDTO;

public interface StartInterviewService {
  public Integer startInterview (String token);
  public List<String> fetchInterviewQuestions (String token);
  public void initiatigInterview (UserAIChatRequestDTO userAIChatRequestDTO,Principal principal);
}
