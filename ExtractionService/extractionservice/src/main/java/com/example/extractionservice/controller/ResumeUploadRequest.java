package com.example.extractionservice.controller;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ResumeUploadRequest {
    private MultipartFile file;
}
