package com.example.developmentcodeeditor.service;

import java.util.List;

import com.example.developmentcodeeditor.requestDTO.DevelopmentEnvironmentReponseDTO;
import com.example.developmentcodeeditor.requestDTO.DevelopmentQuestionResponseDTO;

public interface SetUpDevelopmentEnvironmentService {
  public List<DevelopmentEnvironmentReponseDTO> fetchDevelopmentEnvironment (Integer id);
  public List<DevelopmentQuestionResponseDTO> fetchDevelopmentQuestions();
}