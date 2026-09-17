package com.example.aiinterview.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Void> getMethodName(@RequestParam String token) {
    APIResponseDTO apiResponseDTO = validateLinkServiceImpl.validateLink(token);

    String redirectTarget = "Link is valid".equals(apiResponseDTO.getMessage())
        ? "/interview.html?token=" + token
        : "/link-expired.html";

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(redirectTarget))
        .build();
  }

}
