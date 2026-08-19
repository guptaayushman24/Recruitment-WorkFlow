package com.example.extractionservice.serviceimpl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.extractionservice.ai.AIAssistant;
import com.example.extractionservice.ai.ResumeExtraction;
import com.example.extractionservice.service.ExtractionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionServiceImpl implements ExtractionService{
  private final AIAssistant aiAssistant;
  private final EmbeddingModel embeddingModel;

  @Async
  @Override
  public CompletableFuture<ResumeExtraction> extractSkillProjectComponentExpierence(byte[] resumeBytes) {
    String text = "";
    try (PDDocument document = Loader.loadPDF(resumeBytes)) {
      text = new PDFTextStripper().getText(document);
    }
    catch (Exception e){
      log.error("Error occur in file upload :::: {}",e);
      return CompletableFuture.completedFuture(null);
    }

    ResumeExtraction resumeExtraction = aiAssistant.extractSections(text);
    log.info("Resume extraction finished :::: {}", resumeExtraction);
    log.info("Extraction Skill {}",resumeExtraction.getSkills());
    log.info("Project Skill {}",resumeExtraction.getProjectComponents());
    log.info(" Experience {}",resumeExtraction.getExperience());

       // Converting all the three list into number from 0 to 1
    List<float []> skillsEmbedding = embeddingModel.embed(resumeExtraction.getSkills());

     List<float []> projectEmbedding = embeddingModel.embed(resumeExtraction.getProjectComponents());

     List<float []> experienceEmbedding = embeddingModel.embed(resumeExtraction.getExperience());

     if (skillsEmbedding!=null && projectEmbedding!=null && experienceEmbedding!=null){
        // Save in the database find the weighted average of all the embeddings 
     }
     else{
       log.error("Some error occured while generatnig the embedding");
     }

    return CompletableFuture.completedFuture(resumeExtraction);
    
   
  }

}