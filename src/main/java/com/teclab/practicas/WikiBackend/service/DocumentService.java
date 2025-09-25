package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;

import java.util.List;

public interface DocumentService {

    DocumentDetailResponseDto getDocumentById(Long id);
    DocumentDetailResponseDto createDocument(DocumentRequestDto request);
    DocumentDetailResponseDto updateDocument(Long id, DocumentRequestDto request);
    void deleteDocument(Long id);

    List<DocumentDetailResponseDto> getAllDocuments(String type, String folder);
}
