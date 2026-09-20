package com.example.mailservice.service;

import com.example.mailservice.dto.EmailDTO;
import com.example.mailservice.dto.SecureLinkToken;
import com.example.mailservice.dto.UserInterviewReport;

public interface EmailService {
  public void sendEmailToUser(EmailDTO emailDTO);
  public void sendInterviewActivationLinkToUser (SecureLinkToken secureLinkToken);
  public void sendUserScore (UserInterviewReport userInterviewReport);
}
