package com.example.aiinterview.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Tracks "which question is next" per interview token between stateless STOMP
 * messages. In-memory only - fine for one instance/testing, won't survive a
 * restart or scale across multiple app instances.
 */
@Component
public class InterviewSessionStore {
  private final Map<String, Session> sessions = new ConcurrentHashMap<>();

  public static class Session {
    private final List<String> questions;
    private final AtomicInteger index = new AtomicInteger(0);

    public Session(List<String> questions) {
      this.questions = questions;
    }

    public int currentIndex() {
      return index.get();
    }

    public String currentQuestion() {
      return questions.get(index.get());
    }

    public boolean hasNext() {
      return index.get() < questions.size();
    }

    public void advance() {
      index.incrementAndGet();
    }
  }

  public boolean exists(String token) {
    return sessions.containsKey(token);
  }

  public Session start(String token, List<String> questions) {
    Session session = new Session(questions);
    sessions.put(token, session);
    return session;
  }

  public Session get(String token) {
    return sessions.get(token);
  }

  public void end(String token) {
    sessions.remove(token);
  }
}
