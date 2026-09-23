package com.example.codeeditor.serviceimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.codeeditor.constant.Constant;
import com.example.codeeditor.requestdto.RunCodeRequestDTO;
import com.example.codeeditor.requestdto.SampleCodingTestCaseDTO;
import com.example.codeeditor.service.ValidateRunCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateRunCodeServiceImpl implements ValidateRunCode{

  private final RedisTemplate<Object, Object> sampleTestCaseOutput;

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

    String programmingInput = String.valueOf(runCodeRequestDTO.getProgrammingInput());
    ProcessResult runResult = runProcess(runBuilder, programmingInput);
    log.info("Program output :::: {}", runResult.output());

    // Fetch the expected output cached in redis (StartCodingRoundServiceImpl caches it keyed by test case input)
    Object expectedOutput = sampleTestCaseOutput.opsForValue().get(runCodeRequestDTO.getProgrammingInput());

    SampleCodingTestCaseDTO result = new SampleCodingTestCaseDTO();
    result.setInput(runCodeRequestDTO.getProgrammingInput());
    result.setOutput(runResult.output());

    if (expectedOutput == null){
      log.info("No cached expected output found for input :::: {}", programmingInput);
      result.setIsTestCasePassedORFailed(0);
    } else {
      boolean isPassed = String.valueOf(expectedOutput).trim().equals(runResult.output().trim());
      result.setIsTestCasePassedORFailed(isPassed ? 1 : 0);
    }

    return result;
  }

}
