package com.example.codeeditor.serviceimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.codeeditor.constant.Constant;
import com.example.codeeditor.repository.StartCodingInterviewRepository;
import com.example.codeeditor.requestdto.CodingQuestionStatusDTO;
import com.example.codeeditor.requestdto.RunCodeRequestDTO;
import com.example.codeeditor.requestdto.SampleCodingTestCaseDTO;
import com.example.codeeditor.service.ValidateRunCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateRunCodeServiceImpl implements ValidateRunCode{

  private final RedisTemplate<Object, Object> sampleTestCaseOutput;

  private final ObjectMapper objectMapper;

  private final StartCodingInterviewRepository startCodingInterviewRepository;

  public String sourceCodeFilePath (String sourceCode,String programmingLanguage) throws IOException {
    String extension = programmingLanguage.startsWith(".") ? programmingLanguage.substring(1) : programmingLanguage;

    Path folderPath = Path.of("generated-code", extension);
    Files.createDirectories(folderPath);

    String fileName = resolveFileName(sourceCode, extension);
    Path filePath = folderPath.resolve(fileName + "." + extension);
    Files.writeString(filePath, sourceCode);

    return filePath.toAbsolutePath().toString();
  }

  private String resolveFileName(String sourceCode, String extension) {
    if ("java".equals(extension)) {
      Matcher matcher = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(sourceCode);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return "Source";
  }

  private String stdinFormat(Object value) {
    StringBuilder builder = new StringBuilder();
    appendValue(builder, value);
    return builder.toString();
  }

  private void appendValue(StringBuilder builder, Object value) {
    if (value instanceof List<?> list) {
      builder.append(list.size()).append('\n');
      appendListBody(builder, list);
    } else if (value instanceof Map<?, ?> map) {
      appendMap(builder, map);
    } else {
      // Scalar: int, string, double, boolean
      builder.append(String.valueOf(value)).append('\n');
    }
  }

  private void appendListBody(StringBuilder builder, List<?> list) {
    boolean isNestedArray = !list.isEmpty() && list.get(0) instanceof List;
    if (isNestedArray) {
      // 2D (or deeper) — recurse per row, each row prints its own length + elements
      for (Object row : list) {
        appendValue(builder, row);
      }
    } else {
      // 1D — one space-separated elements line
      String elements = list.stream()
          .map(String::valueOf)
          .collect(Collectors.joining(" "));
      builder.append(elements).append('\n');
    }
  }

  // Postgres jsonb does not preserve object key order, so a field's position in the map
  // can't be relied on. A "<field>Len" sibling key (added by the seed data for
  // unambiguous single string/array fields) is looked up by name instead, and its value is
  // printed immediately before that field's content rather than trusting map iteration order.
  private void appendMap(StringBuilder builder, Map<?, ?> map) {
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = String.valueOf(entry.getKey());
      if (key.endsWith("Len") && isLengthCompanion(map, key)) {
        continue; // printed alongside its paired field below, not on its own
      }

      Object fieldValue = entry.getValue();
      Object explicitLength = (fieldValue instanceof String || fieldValue instanceof List)
          ? map.get(key + "Len")
          : null;

      if (explicitLength != null) {
        builder.append(String.valueOf(explicitLength)).append('\n');
        if (fieldValue instanceof List<?> list) {
          appendListBody(builder, list);
        } else {
          builder.append(String.valueOf(fieldValue)).append('\n');
        }
      } else {
        appendValue(builder, fieldValue);
      }
    }
  }

  private boolean isLengthCompanion(Map<?, ?> map, String lengthKey) {
    String baseKey = lengthKey.substring(0, lengthKey.length() - "Len".length());
    Object baseValue = map.get(baseKey);
    return baseValue instanceof String || baseValue instanceof List;
  }

  private record ProcessResult(int exitCode, String output) {}

  private ProcessResult runProcess(ProcessBuilder processBuilder, String input) throws IOException, InterruptedException {
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    if (input != null && !input.isEmpty()) {
      try (var stdin = process.getOutputStream()) {
        stdin.write(input.getBytes());
        stdin.flush();
      }
    }

    String output;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      output = reader.lines().collect(Collectors.joining("\n"));
    }

    int exitCode = process.waitFor();
    return new ProcessResult(exitCode, output);
  }

  @Override
  public SampleCodingTestCaseDTO validateRunCodeRequest(RunCodeRequestDTO runCodeRequestDTO) throws Exception{
    Constant constant = new Constant();
    HashSet<String> validProgrammingLanguage = constant.nameOfProgrammingLanguagesAllowed;
    String programmingLanguage = runCodeRequestDTO.getProgrammingLanguage();
    // Set from the token below; null when no token was sent. Used when the round-2 score is stored.
    Integer userId = null;
    Integer appliedJobId = null;

    // Validate the programming language
    if (!validProgrammingLanguage.contains(programmingLanguage)){
      // Invalid programming language
      log.info("Invalid Programming Language :::: {}",programmingLanguage);
      SampleCodingTestCaseDTO invalidLanguageResult = new SampleCodingTestCaseDTO();
      invalidLanguageResult.setOutput("Invalide Programming Language !!!!!");
      invalidLanguageResult.setIsTestCasePassedORFailed(0);
      return invalidLanguageResult;
    }

    if (!programmingLanguage.equals(".java") && !programmingLanguage.equals(".cpp")){
      SampleCodingTestCaseDTO unsupportedLanguageResult = new SampleCodingTestCaseDTO();
      unsupportedLanguageResult.setOutput("Execution is not supported yet for " + programmingLanguage);
      unsupportedLanguageResult.setIsTestCasePassedORFailed(0);
      return unsupportedLanguageResult;
    }

    // Resolve who is running the code from the round-2 link token (never from client-sent ids)
    if (runCodeRequestDTO.getToken() != null && !runCodeRequestDTO.getToken().isBlank()) {
      Optional<StartCodingInterviewRepository.UserJob> userJob =
          startCodingInterviewRepository.fetchUserAndJobByToken(runCodeRequestDTO.getToken());
      if (userJob.isEmpty() || userJob.get().round() != 2) {
        SampleCodingTestCaseDTO invalidTokenResult = new SampleCodingTestCaseDTO();
        invalidTokenResult.setOutput("Invalid coding round link");
        invalidTokenResult.setIsTestCasePassedORFailed(0);
        return invalidTokenResult;
      }
      userId = userJob.get().userId();
      appliedJobId = userJob.get().appliedJobId();
      log.info("Run code requested by userId={}, appliedJobId={}", userId, appliedJobId);
    }

    // Write the source code to a file under generated-code/<extension>/
    String filePath = sourceCodeFilePath(runCodeRequestDTO.getSourceCode(), programmingLanguage);
    Path sourcePath = Path.of(filePath);
    Path folderPath = sourcePath.getParent();
    String fileName = sourcePath.getFileName().toString();
    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

    ProcessBuilder compileBuilder = new ProcessBuilder();
    Path executableBinary = null;

    if (programmingLanguage.equals(".java")){
      compileBuilder.command("javac", sourcePath.toString());
    } else {
      executableBinary = folderPath.resolve(baseName);
      compileBuilder.command("g++", sourcePath.toString(), "-o", executableBinary.toString());
    }

    ProcessResult compileResult = runProcess(compileBuilder, null);
    if (compileResult.exitCode() != 0){
      log.info("Compilation failed :::: {}", compileResult.output());
      SampleCodingTestCaseDTO compileFailureResult = new SampleCodingTestCaseDTO();
      compileFailureResult.setOutput("Compilation failed" + " " + compileResult.output());
      compileFailureResult.setIsTestCasePassedORFailed(0);
      recordTestCaseResult(runCodeRequestDTO.getToken(), userId, appliedJobId, runCodeRequestDTO.getProgrammingInput(), false);
      return compileFailureResult;
    }

    // Search the file in the folder generated-code/<extension> and execute the programme
    ProcessBuilder runBuilder = new ProcessBuilder();
    if (programmingLanguage.equals(".java")){
      runBuilder.command("java", "-cp", folderPath.toString(), baseName);
    }
     else {
      runBuilder.command(executableBinary.toString());
    }

    String programmingInput = stdinFormat(runCodeRequestDTO.getProgrammingInput());
    ProcessResult runResult = runProcess(runBuilder, programmingInput);
    log.info("Program output :::: {}", runResult.output());

    // Fetch the expected output cached in redis (StartCodingRoundServiceImpl caches it keyed by a JSON-serialized test case input)
    String redisKey = objectMapper.writeValueAsString(runCodeRequestDTO.getProgrammingInput());
    Object expectedOutput = sampleTestCaseOutput.opsForValue().get(redisKey);

    log.info("Reddis key is :::::: {}",redisKey);
    SampleCodingTestCaseDTO result = new SampleCodingTestCaseDTO();
    result.setInput(runCodeRequestDTO.getProgrammingInput());
    result.setOutput(runResult.output());

    if (expectedOutput == null){
      log.info("No cached expected output found for redis key :::: {}", redisKey);
      result.setIsTestCasePassedORFailed(0);
    } else {
      boolean isPassed = String.valueOf(expectedOutput).trim().equals(runResult.output().trim());
      result.setIsTestCasePassedORFailed(isPassed ? 1 : 0);

      // In future will
    }


    recordTestCaseResult(runCodeRequestDTO.getToken(), userId, appliedJobId, runCodeRequestDTO.getProgrammingInput(),
        result.getIsTestCasePassedORFailed() == 1);

    return result;
  }

  private static final int SECOND_ROUND = 2;
  private static final int SCORE_PENDING_STATUS = 1;
  // 5 test cases per question x 2 points = a score out of 10
  private static final int POINTS_PER_PASSED_TEST_CASE = 2;

  // /runcode checks one test case per call, so each candidate's results are kept per token in a redis
  // hash (test case input -> 1 passed / 0 failed). Re-running a test case overwrites its entry instead of
  // adding to it, so the score can't go past 10, and one candidate's runs never touch another's.
  private void recordTestCaseResult(String token, Integer userId, Integer appliedJobId, Object programmingInput, boolean passed) {
    if (token == null || token.isBlank() || userId == null) {
      return; // no round-2 token, so there is no candidate to store a score for
    }

    String hashKey = "codingRound:result:" + token;
    sampleTestCaseOutput.opsForHash().put(hashKey, objectMapper.writeValueAsString(programmingInput), passed ? 1 : 0);
    sampleTestCaseOutput.expire(hashKey, Duration.ofDays(5)); // same lifetime as the link

    Map<Object, Object> results = sampleTestCaseOutput.opsForHash().entries(hashKey);
    int passedTestCase = (int) results.values().stream().filter(v -> ((Number) v).intValue() == 1).count();

    CodingQuestionStatusDTO codingQuestionStatusDTO = CodingQuestionStatusDTO.builder()
        .totalTestCase(results.size())
        .passedTestCase(passedTestCase)
        .failedTestCase(results.size() - passedTestCase)
        .build();
    log.info("Total Number of test cases ::::: {}", codingQuestionStatusDTO.getTotalTestCase());
    log.info("Passed Number of test cases ::::: {}", codingQuestionStatusDTO.getPassedTestCase());
    log.info("Failed Number of test cases :::::: {}", codingQuestionStatusDTO.getFailedTestCase());

    // recruitment_workflow.user_interview_score, status 1 = pending, round 2 = coding round
    startCodingInterviewRepository.insertIntoUserInterviewScore(userId, appliedJobId,
        codingQuestionStatusDTO.getPassedTestCase() * POINTS_PER_PASSED_TEST_CASE, SCORE_PENDING_STATUS, SECOND_ROUND);
  }

}
