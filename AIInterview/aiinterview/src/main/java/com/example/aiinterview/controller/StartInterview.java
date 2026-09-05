package com.example.aiinterview.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.aiinterview.dto.StartInterviewRequestDTO;
import com.example.aiinterview.dto.UserAIChatRequestDTO;
import com.example.aiinterview.dto.UserValidatedResponseDTO;
import com.example.aiinterview.serviceimpl.StartInterviewServiceImpl;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor 
public class StartInterview {
  private final StartInterviewServiceImpl startInterviewServiceImpl;
  @PostMapping("/startInterview")
  public UserValidatedResponseDTO postMethodName(@RequestBody StartInterviewRequestDTO startInterviewRequestDTO) {
    UserValidatedResponseDTO userValidatedResponseDTO = new UserValidatedResponseDTO();

    Integer userValidatedForStartingInterview = startInterviewServiceImpl.startInterview(startInterviewRequestDTO.getToken());

    if (userValidatedForStartingInterview==1){
      // Fetch the questions for the interview before establishing the web socket connection
      List<String> userInterviewQuestions = startInterviewServiceImpl.fetchInterviewQuestions(startInterviewRequestDTO.getToken());
      
      userValidatedResponseDTO.setIsUserValidated(userValidatedForStartingInterview);
      userValidatedResponseDTO.setMessage("Interview will start shortly");
      userValidatedResponseDTO.setQuestions(userInterviewQuestions);

      return userValidatedResponseDTO;

      //  // Establish the web socket between the AI model and user
      
    }
     userValidatedResponseDTO.setIsUserValidated(0);
     userValidatedResponseDTO.setMessage("Some issue occur please contact to the support team");

     return userValidatedResponseDTO;

  }

  @MessageMapping ("/chat")
  public void handle (@Payload UserAIChatRequestDTO userAIChatRequestDTO,Principal principal){
    startInterviewServiceImpl.initiatigInterview(userAIChatRequestDTO, principal);
  }


}
