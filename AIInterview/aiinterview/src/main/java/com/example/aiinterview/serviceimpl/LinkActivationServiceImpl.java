package com.example.aiinterview.serviceimpl;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.aiinterview.constant.CONSTANT;
import com.example.aiinterview.dto.SecureLinkToken;
import com.example.aiinterview.repository.SaveQuestionRepository;
import com.example.aiinterview.service.LinkActivationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class LinkActivationServiceImpl implements LinkActivationService{
  private final SaveQuestionRepository saveQuestionRepository;
  private final PubliserService publiserService;
  private final CONSTANT constant;
  @Override
  public void createAndSendSecureLink(Integer userId,Integer appliedJobId,String emailAddress) {
    // Interview question generate save the link in the database trigger one scheduler of 
    // 30 mins and pick each user_id and send to the email address will add one more column in the 
    // user link table
    
    String storedInformation = userId+"/"+appliedJobId;

    if (saveQuestionRepository.userInterViewLinkExists(userId, storedInformation)){
      log.info("Interview link already exists for userId={}, appliedJobId={}, skipping duplicate creation", userId, appliedJobId);
      return;
    }

    //String token = UUID.randomUUID().toString();
    String token = userId+"?"+appliedJobId;
    SecureLinkToken secureToken = new SecureLinkToken();
    secureToken.setToken(token);
    secureToken.setStoredInformation(storedInformation);
    secureToken.setExpiryDate(LocalDateTime.now().plusMinutes(7200));
    String secureUrl = "http://localhost:8081/api/links/process?token=" + token;
    secureToken.setLink(secureUrl);
    secureToken.setEmailAddress(emailAddress);

    log.info("Value of token is :::: {}",token);

    // Save the whole detail in the repository
    int isLinkGenerated = saveQuestionRepository.generateUserInterViewLink(secureToken,userId,appliedJobId,constant.PENDING);
    int isActivationLinkAlreadySendToUser = saveQuestionRepository.checkIntivitationAlreadySendToUser(userId,appliedJobId);

    if (isLinkGenerated==1 && isActivationLinkAlreadySendToUser==1){
      // send the mail to the user with the link using mailservice
      // Check in the if statement before sending the message in the publisher check where the status is 1 or not
      byte [] jsonEmailActivationLinkBytes;
      try{
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        jsonEmailActivationLinkBytes = objectMapper.writeValueAsBytes(secureToken);

        log.info("Pushed message ");
      }
      catch (JsonProcessingException e){
        log.error("Error in the AI service generating link :::::: {}",e);
        throw new UncheckedIOException("Failed to serialize match payload for pub-sub", e);
      }

       ByteString data = ByteString.copyFrom(jsonEmailActivationLinkBytes);
       PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
       .setData(data)
       .build();

       publiserService.sendMessageToAIInterviewTopic(pubsubMessage);
       log.info("Message is pushed in the topic user-interview-activation");
       // Filp the staus in the recruitment_workflow.user_link table back to 2 so the next time sheduler would not send the link again to the same user
       saveQuestionRepository.updateUserActivationLink(constant.SUCCESSS,userId, appliedJobId);
    }
  }
}