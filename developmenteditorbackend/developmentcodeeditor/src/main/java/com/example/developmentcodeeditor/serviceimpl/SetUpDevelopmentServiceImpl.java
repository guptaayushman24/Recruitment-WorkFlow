package com.example.developmentcodeeditor.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.developmentcodeeditor.repository.DevelopmentEnvironmentRepository;
import com.example.developmentcodeeditor.repository.DevelopmentQuestionRepository;
import com.example.developmentcodeeditor.requestDTO.DevelopmentEnvironmentReponseDTO;
import com.example.developmentcodeeditor.requestDTO.DevelopmentQuestionResponseDTO;
import com.example.developmentcodeeditor.service.SetUpDevelopmentEnvironmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetUpDevelopmentServiceImpl implements SetUpDevelopmentEnvironmentService{
  private  final DevelopmentEnvironmentRepository developmentEnvironmentRepository;
  private  final DevelopmentQuestionRepository developmentQuestionRepository;
  @Override
  public List<DevelopmentEnvironmentReponseDTO> fetchDevelopmentEnvironment(Integer id) {
    return developmentEnvironmentRepository.fetchFileContentTypeContent(id);
  }

  @Override
  public List<DevelopmentQuestionResponseDTO> fetchDevelopmentQuestions() {
    return developmentQuestionRepository.fetchDevelopmentQuestions();
  }

}
