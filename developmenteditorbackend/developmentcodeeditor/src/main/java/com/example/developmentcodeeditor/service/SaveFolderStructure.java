package com.example.developmentcodeeditor.service;

import java.io.IOException;

import com.example.developmentcodeeditor.requestDTO.FileNodeDTO;

public interface SaveFolderStructure {
   public void saveDirectory(FileNodeDTO node, String currentPath) throws IOException;
}