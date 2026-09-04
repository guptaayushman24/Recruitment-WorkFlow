package com.example.aiinterview.serviceimpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.aiinterview.dto.APIResponseDTO;
import com.example.aiinterview.dto.TokenStatusExpiryDTO;
import com.example.aiinterview.repository.ValidateLinkRepository;
import com.example.aiinterview.service.ValidateLinkService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateLinkServiceImpl implements ValidateLinkService{
  private final ValidateLinkRepository validateLinkRepository;
  @Override
  public APIResponseDTO validateLink(String token) {
    TokenStatusExpiryDTO tokenStatusExpiryDTO =  validateLinkRepository.fetchStatusAndExpiryDate(token);

    APIResponseDTO apiResponseDTO = new APIResponseDTO();

    if (tokenStatusExpiryDTO.getExpiryDate().isAfter(LocalDateTime.now())){
      // link is valid
      apiResponseDTO.setMessage("Link is valid");
    }
    else{
      // link is invalid
      apiResponseDTO.setMessage("Link is invalid");
    }

    return apiResponseDTO;
  }
  }
