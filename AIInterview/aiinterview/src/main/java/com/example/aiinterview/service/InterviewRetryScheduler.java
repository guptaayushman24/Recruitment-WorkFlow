package com.example.aiinterview.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.aiinterview.dto.UserIdAppliedJobId;
import com.example.aiinterview.repository.SaveQuestionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Recovery for matched candidates who never got their interview questions / link - e.g. Ollama was
 * down when the Pub/Sub message arrived. That message is acked before the LLM call, so Pub/Sub will
 * not redeliver it. Candidates are found from the database (matched, but no user_question rows or no
 * user_link), so nothing about their matched status has to change.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewRetryScheduler {
  private final SaveQuestionRepository saveQuestionRepository;
  private final SubScriberService subScriberService;

  @Scheduled(
      initialDelayString = "${interview.retry.initial-delay-ms:30000}",
      fixedDelayString = "${interview.retry.interval-ms:300000}")
  public void retryMissingInterviews() {
    List<UserIdAppliedJobId> pending = saveQuestionRepository.findMatchesMissingInterview();
    if (pending.isEmpty()) {
      return;
    }

    log.info("Retrying interview creation for {} matched candidate(s) with no questions/link", pending.size());
    for (UserIdAppliedJobId match : pending) {
      try {
        subScriberService.createQuestionsAndLink(
            saveQuestionRepository.fetchResumeAndJobDetail(match.getUserId(), match.getAppliedJobId()));
      } catch (Exception e) {
        // One candidate failing (e.g. Ollama still down) must not stop the others; next run retries
        log.warn("Retry failed for userId={}, appliedJobId={}: {}", match.getUserId(), match.getAppliedJobId(), e.toString());
      }
    }
  }
}
