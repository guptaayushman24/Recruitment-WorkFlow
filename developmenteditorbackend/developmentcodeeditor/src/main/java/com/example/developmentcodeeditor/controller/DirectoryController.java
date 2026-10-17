package com.example.developmentcodeeditor.controller;

import java.nio.file.Paths;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.developmentcodeeditor.requestDTO.DevelopmentEnvironmentReponseDTO;
import com.example.developmentcodeeditor.requestDTO.DevelopmentQuestionResponseDTO;
import com.example.developmentcodeeditor.requestDTO.FileNodeDTO;
import com.example.developmentcodeeditor.service.DockerRunService;
import com.example.developmentcodeeditor.service.SaveFolderStructure;
import com.example.developmentcodeeditor.service.SetUpDevelopmentEnvironmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class DirectoryController {
    private final SaveFolderStructure saveFolderStructure;
    private final SetUpDevelopmentEnvironmentService setUpDevelopmentEnvironmentService;
    private final DockerRunService dockerRunService;

    @Value("${storage.base-path}")
    private String targetBasePath;

    @PostMapping("/upload") // Call these API when user triggers the run button
    public ResponseEntity<String> uploadDirectory(
            @RequestBody FileNodeDTO fileNodeDTO, @RequestParam(required = false) Integer questionId) {
        try {
            saveFolderStructure.saveDirectory(fileNodeDTO, targetBasePath);
            String runId = fileNodeDTO.getName().toLowerCase().replaceAll("[^a-z0-9_.-]", "-");
            String projectDir = Paths.get(targetBasePath, fileNodeDTO.getName()).toString();
            int hostPort = dockerRunService.buildAndRun(runId, projectDir,questionId);
            return ResponseEntity.ok("Directory saved and app running on http://localhost:" + hostPort);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/setupdevelopmentenvironment")
    public ResponseEntity<List<DevelopmentEnvironmentReponseDTO>> getMethodName(@RequestParam Integer projectId) {
        return ResponseEntity.ok(setUpDevelopmentEnvironmentService.fetchDevelopmentEnvironment(projectId));

    }

    @PostMapping("/getDevelopmentMockTask")
    public ResponseEntity<List<DevelopmentQuestionResponseDTO>> postMethodName() {
        // @RequestBody DevelopmentQuestionDTO developmentQuestionDTO
     return ResponseEntity.ok(setUpDevelopmentEnvironmentService.fetchDevelopmentQuestions());
    }
    
}
