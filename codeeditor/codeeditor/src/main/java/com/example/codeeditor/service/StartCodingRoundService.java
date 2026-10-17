package com.example.codeeditor.service;

import java.util.List;

import com.example.codeeditor.responsedto.CodingQuestion;
import com.example.codeeditor.responsedto.CodingRoundLinkResponseDTO;

public interface StartCodingRoundService {
  List<CodingQuestion> startCodingRound();

  CodingRoundLinkResponseDTO validateCodingRoundLink(String token);
}
