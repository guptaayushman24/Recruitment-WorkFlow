package com.example.extractionservice.serviceimpl;

import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.extractionservice.ai.AIAssistant;
import com.example.extractionservice.ai.ResumeExtraction;
import com.example.extractionservice.repository.SaveUserDetail;
import com.example.extractionservice.repository.SaveUserEmbedding;
import com.example.extractionservice.service.ExtractionService;

import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionServiceImpl implements ExtractionService{
  private final AIAssistant aiAssistant;
  private final EmbeddingModel embeddingModel;
  private final SaveUserDetail saveUserDetail;
  private final SaveUserEmbedding saveUserEmbedding;

  @Async
  @Override
  public CompletableFuture<ResumeExtraction> extractSkillProjectComponentExpierence(byte[] resumeBytes,String userEmail) {
    String text = "";
    try (PDDocument document = Loader.loadPDF(resumeBytes)) {
      text = new PDFTextStripper().getText(document);
    }
    catch (Exception e){
      log.error("Error occur in file upload :::: {}",e);
      return CompletableFuture.completedFuture(null);
    }

    ResumeExtraction resumeExtraction = aiAssistant.extractSections(text);
   /*  log.info("Resume extraction finished :::: {}", resumeExtraction);
    log.info("Extraction Skill {}",resumeExtraction.getSkills());
    log.info("Project Skill {}",resumeExtraction.getProjectComponents());
    log.info(" Experience {}",resumeExtraction.getExperience()); */

    // Each section's list of strings is joined into one block of text so that
    // embed() returns a single vector representing the whole section.
    float[] skillsEmbedding = embeddingModel.embed(String.join(", ", resumeExtraction.getSkills())).content().vector();
    float[] projectEmbedding = embeddingModel.embed(String.join(", ", resumeExtraction.getProjectComponents())).content().vector();
    float[] experienceEmbedding = embeddingModel.embed(String.join(", ", resumeExtraction.getExperience())).content().vector();

    float[] weightedEmbedding = weightedAverage(skillsEmbedding, projectEmbedding, experienceEmbedding);
    log.info("Weighted resume embedding :::: {}", weightedEmbedding);

    if (weightedEmbedding.length>0){
      // Feth the userId from the userEmail and insert in the table
       Integer userId = saveUserDetail.fetchUserIdFromEmail(userEmail);
       saveUserEmbedding.saveUserEmbedding(userId,weightedEmbedding,11,0);
    }
    return CompletableFuture.completedFuture(resumeExtraction);
  }

  private static final float SKILLS_WEIGHT = 0.5f;
  private static final float PROJECTS_WEIGHT = 0.2f;
  private static final float EXPERIENCE_WEIGHT = 0.3f;

  private float[] weightedAverage(float[] skills, float[] projects, float[] experience) {
    float[] result = new float[skills.length];
    for (int i = 0; i < skills.length; i++) {
      result[i] = skills[i] * SKILLS_WEIGHT + projects[i] * PROJECTS_WEIGHT + experience[i] * EXPERIENCE_WEIGHT;
    }
    return result;
  }

}