package com.example.extractionservice.serviceimpl;

import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.extractionservice.ai.AIAssistant;
import com.example.extractionservice.ai.AIAssistantJobDescription;
import com.example.extractionservice.ai.ResumeExtraction;
import com.example.extractionservice.constant.CONSTANT;
import com.example.extractionservice.dto.ExtractionResumeJobDescriptionDTO;
import com.example.extractionservice.dto.ExtractionResumeJobDescriptionDTO.JobDescription;
import com.example.extractionservice.dto.ExtractionResumeJobDescriptionDTO.Resume;
import com.example.extractionservice.repository.SaveJobEmbedding;
import com.example.extractionservice.repository.SaveUserDetail;
import com.example.extractionservice.repository.SaveUserEmbedding;
import com.example.extractionservice.service.ExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.cloud.spring.pubsub.core.PubSubTemplate;

import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionServiceImpl implements ExtractionService{
   private static final float SKILLS_WEIGHT = 0.5f;
  private static final float PROJECTS_WEIGHT = 0.2f;
  private static final float EXPERIENCE_WEIGHT = 0.3f;

  private final AIAssistant aiAssistant;
  private final AIAssistantJobDescription aiAssistantJobDescription;
  private final EmbeddingModel embeddingModel;
  private final SaveUserDetail saveUserDetail;
  private final SaveUserEmbedding saveUserEmbedding;
  private final SaveJobEmbedding saveJobEmbedding;
  private final CONSTANT constant;

  @Async
  @Override
  public CompletableFuture<ResumeExtraction> extractSkillProjectComponentExpierence(byte[] resumeBytes,String userEmail) {
     // Send to the pub-sub
    ExtractionResumeJobDescriptionDTO extractionResumeJobDescriptionDTO = new ExtractionResumeJobDescriptionDTO();
    ObjectMapper objectMapper = new ObjectMapper();
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
    
    Resume resume = new Resume();
    resume.setExperience(resumeExtraction.getExperience());
    resume.setSkills(resumeExtraction.getSkills());
    resume.setProjectComponents(resumeExtraction.getProjectComponents());

    extractionResumeJobDescriptionDTO.setResumeExtraction(resume);
    // With the help of the user_email insert the skills,experiece and project_component


    float[] weightedEmbedding = weightedAverage(skillsEmbedding, projectEmbedding, experienceEmbedding);
    // log.info("Weighted resume embedding :::: {}", weightedEmbedding);

    if (weightedEmbedding.length>0){
      // Feth the userId from the userEmail and insert in the table
       Integer userId = saveUserDetail.fetchUserIdFromEmail(userEmail);
       saveUserDetail.updateUserDate(userId, resumeExtraction.getSkills(), resume.getExperience(), resume.getProjectComponents());
       saveUserEmbedding.saveUserEmbedding(userId,weightedEmbedding,constant.PENDING,0);
    }
    return CompletableFuture.completedFuture(resumeExtraction);
  }


  private float[] weightedAverage(float[] skills, float[] projects, float[] experience) {
    float[] result = new float[skills.length];
    for (int i = 0; i < skills.length; i++) {
      result[i] = skills[i] * SKILLS_WEIGHT + projects[i] * PROJECTS_WEIGHT + experience[i] * EXPERIENCE_WEIGHT;
    }
    return result;
  }

  @Override
  public CompletableFuture<ResumeExtraction> extractSkillProjectComponentExpierenceFromJobDescription(
      String jobDescription) {
        ResumeExtraction jobDescriptionExtraction = aiAssistantJobDescription.extractSections(jobDescription);

        float [] skillsEmbeddingJobDescription = embeddingModel.embed(String.join(",", jobDescriptionExtraction.getSkills())).content().vector();

        float [] projectEmbeddingJobDescription = embeddingModel.embed(
          String.join(",",jobDescriptionExtraction.getProjectComponents())).content().vector();

          float [] expericeEmbeddingJobDescription = embeddingModel.embed(String.join(",",jobDescriptionExtraction.getExperience())).content().vector();

            float[] weightedEmbeddingJobDescription = weightedAverage(skillsEmbeddingJobDescription, projectEmbeddingJobDescription, expericeEmbeddingJobDescription);

            if (weightedEmbeddingJobDescription.length>0){
              // Save Job Embedding Here
              // Add the job_skills,job_experience and job_project_component
              saveJobEmbedding.saveJobEmbedding(jobDescription, weightedEmbeddingJobDescription,jobDescriptionExtraction.getSkills(),jobDescriptionExtraction.getExperience(),jobDescriptionExtraction.getProjectComponents());
            }

            return CompletableFuture.completedFuture(jobDescriptionExtraction);
  }

}