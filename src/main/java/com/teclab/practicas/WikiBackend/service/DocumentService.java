package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentTextRequestDTO;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentTextResponseDTO;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentUrlRequestDto;

import java.util.List;

public interface DocumentService {

    DocumentDetailResponseDto createDocument(DocumentUrlRequestDto requestDto);
    List<DocumentDetailResponseDto> getAllDocuments();
    DocumentDetailResponseDto getDocumentByRoles(Long id); //si es correcto devolver documento sino devolver error
    DocumentDetailResponseDto updateDocument(Long id, DocumentUrlRequestDto requestDto);
    void deleteDocument(Long id);

    DocumentTextResponseDTO createText(DocumentTextRequestDTO documentTextRequestDTO);
    DocumentTextResponseDTO updateText(Long id, DocumentTextRequestDTO documentTextRequestDTO);
    void deleteText(Long id);
    List<DocumentTextResponseDTO> getAllTextDocuments();
    DocumentTextResponseDTO getTextDocument(Long id);
}
