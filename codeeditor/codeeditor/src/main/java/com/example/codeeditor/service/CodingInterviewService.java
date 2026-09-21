package com.example.codeeditor.service;

import java.util.List;

import com.example.codeeditor.responsedto.CodingQuestion;

public interface CodingInterviewService {
  List<CodingQuestion> getCodingQuestions();
}
