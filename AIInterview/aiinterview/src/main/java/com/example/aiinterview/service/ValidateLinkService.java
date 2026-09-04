package com.example.aiinterview.service;

import com.example.aiinterview.dto.APIResponseDTO;

public interface ValidateLinkService {
  public APIResponseDTO validateLink(String token);
}
