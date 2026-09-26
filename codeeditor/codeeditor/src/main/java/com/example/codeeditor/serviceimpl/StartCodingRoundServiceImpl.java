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
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartCodingRoundServiceImpl implements StartCodingRoundService {

  private final StartCodingInterviewRepository startCodingInterviewRepository;

  private final RedisTemplate <Object,Object> sampleTestCaseOutput;

  private final ObjectMapper objectMapper;

  @Override
  public List<CodingQuestion> startCodingRound() {
    int min = 1;
    int max = 164;
    int customRange = min + (int) (Math.random() * ((max - min) + 1));

    List<CodingQuestion> codingQuestions = startCodingInterviewRepository.fetchCodingQuestions(customRange);

    // Cache the expected output of each test case of the returned question, keyed by its JSON input
    // Check if in the reddis key is present then do not need to store in the redis
    for (CodingQuestion codingQuestion : codingQuestions){
      for (SampleCodingTestCaseDTO sampleCodingTestCaseDTO : codingQuestion.getTestCases()){
        String redisKey = objectMapper.writeValueAsString(sampleCodingTestCaseDTO.getInput());
        if (!sampleTestCaseOutput.hasKey(redisKey)){
          sampleTestCaseOutput.opsForValue().set(redisKey, sampleCodingTestCaseDTO.getOutput());
          log.info("Value stored in reddis :::::::: key {} value {}", redisKey, sampleCodingTestCaseDTO.getOutput());
        }
      }
    }
    return codingQuestions;
  }
}