package com.example.aiinterview.ai;

import java.util.List;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InterviewQuestionAssistant {

    @SystemMessage("""
            You are an AI technical interviewer. Given a candidate's resume details
            (skills, project components, experience) and a job description's
            requirements (job skills, job experience, job components), generate a
            focused set of interview questions.
            Rules:
            1. Prioritize questions about skills/technologies that appear in both
               the resume and the job description, to verify the depth of the
               candidate's real experience with them.
            2. Include a few questions targeting job requirements the resume does
               not clearly cover, to assess the candidate's actual familiarity
               with them.
            3. Ask about specific projects and experience entries from the resume,
               not just generic skill definitions.
            4. Base every question strictly on the given resume and job description
               content - do not invent skills, projects, or experience that are
               not mentioned.
            5. Return only the questions themselves, nothing else.
            """)
    @UserMessage("""
            Candidate resume:
            Skills: {{resumeSkills}}
            Project Components: {{resumeProjectComponents}}
            Experience: {{resumeExperience}}

            Job description:
            Required Skills: {{jobSkills}}
            Required Experience: {{jobExperience}}
            Job Components: {{jobComponents}}

            Generate the interview questions as described.
            """)
    InterviewQuestions generateQuestions(
            @V("resumeSkills") List<String> resumeSkills,
            @V("resumeProjectComponents") List<String> resumeProjectComponents,
            @V("resumeExperience") List<String> resumeExperience,
            @V("jobSkills") List<String> jobSkills,
            @V("jobExperience") List<String> jobExperience,
            @V("jobComponents") List<String> jobComponents);
}
