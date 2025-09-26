package com.teclab.practicas.WikiBackend.service.document;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentFileRequestDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;

import java.util.List;

public interface DocumentService {

    DocumentDetailResponseDto createFileDocument(DocumentFileRequestDto request);
    DocumentDetailResponseDto updateFileDocument(Long id, DocumentFileRequestDto request);

    DocumentDetailResponseDto getDocumentById(Long id);
    DocumentDetailResponseDto createDocument(DocumentRequestDto request);
    DocumentDetailResponseDto updateDocument(Long id, DocumentRequestDto request);
    void deleteDocument(Long id);
    List<DocumentDetailResponseDto> getAllDocuments(String type, String folder);
}
