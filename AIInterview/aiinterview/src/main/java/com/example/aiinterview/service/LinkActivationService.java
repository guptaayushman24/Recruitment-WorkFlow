package com.example.aiinterview.service;

public interface LinkActivationService {
  public void createAndSendSecureLink(Integer userId,Integer appliedJobId,String emailAddress);
}
