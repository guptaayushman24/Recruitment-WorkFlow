package com.example.developmentcodeeditor.serviceimpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Service;

import java.io.IOException;

import com.example.developmentcodeeditor.requestDTO.FileNodeDTO;
import com.example.developmentcodeeditor.service.SaveFolderStructure;

import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class SaveFolderStructureServiceImpl implements SaveFolderStructure {

  @Override
  public void saveDirectory(FileNodeDTO node, String currentPath) throws IOException {
    Path path = Paths.get(currentPath, node.getName());

    boolean isDirectory = node.isDirectory() || (node.getChildren() != null && !node.getChildren().isEmpty());

    if (isDirectory) {
      // Create the directory locally
      Files.createDirectories(path);

      // Recursively create children
      if (node.getChildren() != null) {
        for (FileNodeDTO child : node.getChildren()) {
          saveDirectory(child, path.toString());
        }
      }
    } else {
      // Create parent directories if they don't exist
      Files.createDirectories(path.getParent());

      // Write plain-text file content as sent by the frontend
      String fileContent = node.getContent() != null ? node.getContent() : "";

      Files.writeString(path, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
  }
}
