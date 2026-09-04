package com.example.aiinterview.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.aiinterview.dto.APIResponseDTO;
import com.example.aiinterview.serviceimpl.ValidateLinkServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor  
public class ValidateLink {
  private final ValidateLinkServiceImpl validateLinkServiceImpl;
  @GetMapping("/api/links/process")
  public APIResponseDTO getMethodName(@RequestParam String token) {
    return validateLinkServiceImpl.validateLink(token);
  }
  
}
