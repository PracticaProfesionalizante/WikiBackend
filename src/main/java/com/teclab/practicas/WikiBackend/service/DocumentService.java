package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;

import java.util.List;

public interface DocumentService {

    DocumentDetailResponseDto getDocumentByRoles(Long id);
    DocumentDetailResponseDto updateDocument(Long id, DocumentRequestDto request);
    void deleteDocument(Long id);

    DocumentDetailResponseDto createDocument(DocumentRequestDto request);
    List<DocumentDetailResponseDto> getAllUrlDocuments();

    DocumentDetailResponseDto createText(DocumentRequestDto request);
    List<DocumentDetailResponseDto> getAllTextDocuments();
}
