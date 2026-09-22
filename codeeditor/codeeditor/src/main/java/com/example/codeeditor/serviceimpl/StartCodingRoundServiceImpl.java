package com.example.codeeditor.serviceimpl;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.codeeditor.repository.StartCodingInterviewRepository;
import com.example.codeeditor.requestdto.SampleCodingTestCaseDTO;
import com.example.codeeditor.responsedto.CodingQuestion;
import com.example.codeeditor.service.StartCodingRoundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j 
public class StartCodingRoundServiceImpl implements StartCodingRoundService {

  private final StartCodingInterviewRepository startCodingInterviewRepository;

  private final RedisTemplate <Object,Object> sampleTestCaseOutput;

  @Override
  public CodingQuestion startCodingRound() {
    int min = 1;
    int max = 164;
    int customRange = min + (int) (Math.random() * ((max - min) + 1));
    List<SampleCodingTestCaseDTO> sampleCodingTestCase = startCodingInterviewRepository.fetchCodingTestCaseInputOuput(customRange);
    // Iterate on the list and store in the reddis (sampleTestCaseOutput)
    for (SampleCodingTestCaseDTO sampleCodingTestCaseDTO:sampleCodingTestCase){
      sampleTestCaseOutput.opsForValue().set(sampleCodingTestCaseDTO.getInput(), sampleCodingTestCaseDTO.getOutput());
      Object storedValue = sampleTestCaseOutput.opsForValue().get(sampleCodingTestCaseDTO.getInput());
      log.info("Value stored in reddis :::::::: {}", storedValue);
    }
    return startCodingInterviewRepository.fetchCodingQuestions(customRange);
  }
}