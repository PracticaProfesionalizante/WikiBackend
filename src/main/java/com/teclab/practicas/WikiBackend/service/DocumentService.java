package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.entity.Document;

import java.util.List;

public interface DocumentService {

    public Document createDocument(Document document);
    public List<Document> getAllDocuments();
    public Document getDocumentByRoles(Long id); //si es correcto devolver documento sino devolver error
    public Document updateDocument(Long id, Document updatedDocument);
    public void deleteDocument(Long id);
}


// DocumentUrlRequestDto
// DocumentDetailResponseDto