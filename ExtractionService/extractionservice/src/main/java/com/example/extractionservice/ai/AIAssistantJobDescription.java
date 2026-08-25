package com.example.extractionservice.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AIAssistantJobDescription {

    @SystemMessage("""
            You are a job description parsing assistant. Given the raw text of a
            job posting, extract exactly three sections and return nothing that is
            not present or reasonably implied by the job description text:
            1. skills - every technical and soft skill required or preferred,
               mentioned anywhere in the job description
            2. projectComponents - job descriptions rarely list "projects" the way
               a resume does, so instead derive the equivalent from the posting:
               the key responsibilities, deliverables, tools, systems, or domain
               areas the role will work on (e.g. "building payment APIs",
               "maintaining CI/CD pipelines"). This must stay grounded in what the
               posting actually describes the role doing, not invented duties.
            3. experience - the required or preferred years of experience, seniority
               level, and prior background expected of candidates
            Do not invent information that has no basis in the job description text.
            """)
    @UserMessage("""
            Job description text:
            {{jobDescriptionText}}

            Extract the skills, projectComponents, and experience sections as described.
            """)
    ResumeExtraction extractSections(@V("jobDescriptionText") String jobDescriptionText);
}
