package com.example.mailservice.service;

import com.example.mailservice.dto.EmailDTO;

public interface EmailService {
  public void sendEmailToUser(EmailDTO emailDTO);
}
