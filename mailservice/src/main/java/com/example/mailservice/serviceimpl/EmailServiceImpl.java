package com.example.mailservice.serviceimpl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.mailservice.constant.CONSTANT;
import com.example.mailservice.dto.EmailDTO;
import com.example.mailservice.dto.SecureLinkToken;
import com.example.mailservice.dto.UserInterviewReport;
import com.example.mailservice.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{
  private final JavaMailSender javaMailSender;
  @Override
  public void sendEmailToUser(EmailDTO emailDTO) {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    String subject = CONSTANT.MAIL_SUBJECT;
    String greeting = CONSTANT.GREETING;
    String body = greeting +" "+emailDTO.getUserFirstName()+" "+emailDTO.getUserLastName()+" "+CONSTANT.MAIL_BODY;

    simpleMailMessage.setTo(emailDTO.getUserEmailAddress());
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(body);

    javaMailSender.send(simpleMailMessage);
  }
  @Override
  public void sendInterviewActivationLinkToUser(SecureLinkToken secureLinkToken){
     SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
     String subject = CONSTANT.MAIL_SUBJECT_INTERVIEW_LINK;
      String greeting = CONSTANT.GREETING;
      String body = greeting+" "+CONSTANT.MAIL_SUBJECT_INTERVIEW_LINK+" "+secureLinkToken.getLink();

    simpleMailMessage.setTo(secureLinkToken.getEmailAddress());
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(body);
  }
  @Override
  public void sendUserScore(UserInterviewReport userInterviewReport) {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    String subject = CONSTANT.MAIL_SUBJECT_USER_INTERVIEW_SCORE;
    String greeting = CONSTANT.GREETING;
    String body = greeting+" "+String.format(CONSTANT.MAIL_BODY_INTERVIEW_SCORE, userInterviewReport.getJobTitle());

    simpleMailMessage.setTo(userInterviewReport.getEmail());
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(body);

    javaMailSender.send(simpleMailMessage);
  }
  
}
