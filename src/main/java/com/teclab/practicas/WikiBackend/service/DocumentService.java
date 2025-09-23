package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentUrlRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;

import java.util.List;

public interface DocumentService {

    public Document createDocument(DocumentUrlRequestDto requestDto);
    public List<DocumentDetailResponseDto> getAllDocuments();
    public Document getDocumentByRoles(Long id); //si es correcto devolver documento sino devolver error
    public Document updateDocument(Long id, DocumentUrlRequestDto requestDto);
    public void deleteDocument(Long id);
}
