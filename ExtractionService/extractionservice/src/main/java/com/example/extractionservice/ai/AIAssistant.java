package com.example.extractionservice.ai;

import org.springframework.http.ResponseEntity;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AIAssistant {

    @SystemMessage("""
            You are a resume parsing assistant. Given the raw text of a resume,
            extract exactly three sections and return nothing that is not present
            in the resume text:
            1. skills - every technical and soft skill mentioned anywhere in the resume
            2. projectComponents - the key components, technologies and highlights of
               each project listed under a projects section
            3. experience - the responsibilities and achievements from each work
               experience entry
            Do not infer, guess, or add information that is not explicitly stated
            in the resume text.
            """)
    @UserMessage("""
            Resume text:
            {{resumeText}}

            Extract the skills, projectComponents, and experience sections as described.
            """)
   ResumeExtraction  extractSections(@V("resumeText") String resumeText);
}
