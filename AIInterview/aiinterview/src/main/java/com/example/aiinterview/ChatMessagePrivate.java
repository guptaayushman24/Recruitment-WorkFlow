package com.example.aiinterview;

import jakarta.annotation.Generated;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatMessagePrivate {
  private String senderId;
  private String receiverId;
  private String content;
}
