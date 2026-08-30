package com.example.mailservice.service;

import com.example.mailservice.dto.EmailDTO;
import com.example.mailservice.dto.SecureLinkToken;

public interface EmailService {
  public void sendEmailToUser(EmailDTO emailDTO);
  public void sendInterviewActivationLinkToUser (SecureLinkToken secureLinkToken);
}
