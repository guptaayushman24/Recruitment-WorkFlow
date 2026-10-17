package com.example.developmentcodeeditor.requestDTO;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data 
@Getter 
@Setter
public class FileNodeDTO {
   private String name;
    private boolean isDirectory;
    private String content;
    private List<FileNodeDTO> children;
}
