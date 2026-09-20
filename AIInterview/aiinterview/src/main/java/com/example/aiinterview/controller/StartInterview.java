package com.example.aiinterview.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.aiinterview.dto.StartInterviewRequestDTO;
import com.example.aiinterview.dto.UserAIChatRequestDTO;
import com.example.aiinterview.dto.UserValidatedResponseDTO;
import com.example.aiinterview.serviceimpl.StartInterviewServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j 
@RestController
@RequiredArgsConstructor 
public class StartInterview {
  private final StartInterviewServiceImpl startInterviewServiceImpl;
  @PostMapping("/startInterview")
  public UserValidatedResponseDTO postMethodName(@RequestBody StartInterviewRequestDTO startInterviewRequestDTO) throws IOException{
    UserValidatedResponseDTO userValidatedResponseDTO = new UserValidatedResponseDTO();

    Integer userValidatedForStartingInterview = startInterviewServiceImpl.startInterview(startInterviewRequestDTO.getToken());

    if (userValidatedForStartingInterview==1){
      log.info("Is user validated for starting the interview :::::: {}",userValidatedForStartingInterview);
      // Fetch the questions for the interview before establishing the web socket connection
      List<String> userInterviewQuestions = startInterviewServiceImpl.fetchInterviewQuestions(startInterviewRequestDTO.getToken());
      
      userValidatedResponseDTO.setIsUserValidated(userValidatedForStartingInterview);
      userValidatedResponseDTO.setMessage("Interview will start shortly");
      userValidatedResponseDTO.setQuestions(userInterviewQuestions);

      return userValidatedResponseDTO;

      //  // Establish the web socket between the AI model and user
      
    }
     userValidatedResponseDTO.setIsUserValidated(0);

     if (userValidatedForStartingInterview==2){
       userValidatedResponseDTO.setMessage("This interview link has already been used");
     } else if (userValidatedForStartingInterview==3){
       userValidatedResponseDTO.setMessage("This interview link has expired");
     } else {
       userValidatedResponseDTO.setMessage("Some issue occur please contact to the support team");
     }

     return userValidatedResponseDTO;

  }

  @MessageMapping ("/chat")
  public void handle (@Payload UserAIChatRequestDTO userAIChatRequestDTO,Principal principal){
    startInterviewServiceImpl.initiatigInterview(userAIChatRequestDTO, principal);
  }


}
