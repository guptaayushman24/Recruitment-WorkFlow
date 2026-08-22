package com.example.extractionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String resumePath;
}
