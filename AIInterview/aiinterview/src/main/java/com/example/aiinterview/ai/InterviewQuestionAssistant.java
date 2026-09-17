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
            1. Generate exactly 10 questions - no more, no fewer.
            2. Prioritize questions about skills/technologies that appear in both
               the resume and the job description, to verify the depth of the
               candidate's real experience with them.
            3. Include a few questions targeting job requirements the resume does
               not clearly cover, to assess the candidate's actual familiarity
               with them.
            4. Ask about specific projects and experience entries from the resume,
               not just generic skill definitions.
            5. Base every question strictly on the given resume and job description
               content - do not invent skills, projects, or experience that are
               not mentioned.
            6. Each question must be a complete, detailed, well-formed sentence
               that a candidate could answer on its own - never a short/terse
               phrase or fragment (e.g. write "Can you walk me through a time you
               had to resolve a missing vendor contact during a project?", not
               a bare fragment like "vendor missing contact").
            7. Return only the questions themselves, nothing else.
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
