package com.example.aiinterview.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserValidatedResponseDTO {
  private  Integer isUserValidated;
  private String message;
  private List<String> questions;
}
