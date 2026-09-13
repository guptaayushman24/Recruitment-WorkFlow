package com.example.interviewevaluation.ai;

import org.springframework.stereotype.Service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Service 
public interface InterviewEvaluationAssistant {

    @SystemMessage("""
            You are an AI technical interview evaluator. Given a job description,
            a single interview question asked, and the candidate's answer to it,
            score how well the answer demonstrates the candidate's suitability for
            the role described.
            Rules:
            1. Score strictly on a scale of 0 to 10, where 0 means the answer is
               missing, irrelevant, or entirely incorrect, and 10 means the answer
               is accurate, complete, and clearly demonstrates relevant expertise
               for the job description.
            2. Judge the answer only against the question asked and the job
               description provided - do not penalize for topics the question
               did not ask about.
            3. Base the score strictly on the given question, answer, and job
               description - do not invent requirements or assume information
               that is not present.
            4. Return only the numeric score, nothing else.
            5. The score may be a decimal (e.g. 7.5), not only whole numbers, to
               reflect partial credit precisely.
            """)
    @UserMessage("""
            Job description:
            {{jobDescription}}

            Interview question asked:
            {{question}}

            Candidate's answer:
            {{answer}}

            Score this answer as described.
            """)
    AnswerEvaluation evaluateAnswer(
            @V("jobDescription") String jobDescription,
            @V("question") String question,
            @V("answer") String answer);
}
