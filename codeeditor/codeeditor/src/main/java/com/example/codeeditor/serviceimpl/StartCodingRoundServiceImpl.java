package com.example.codeeditor.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.codeeditor.repository.StartCodingInterviewRepository;
import com.example.codeeditor.responsedto.CodingQuestion;
import com.example.codeeditor.service.StartCodingRoundService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StartCodingRoundServiceImpl implements StartCodingRoundService {

  private final StartCodingInterviewRepository startCodingInterviewRepository;

  @Override
  public List<CodingQuestion> startCodingRound() {
    return startCodingInterviewRepository.fetchCodingQuestions();
  }
}
