package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    @Override
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public Document getDocumentByRoles(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enlace no encontrado con ID: " + id));
    }

    @Override
    public Document updateDocument(Long id, Document updatedDocument) {
        return documentRepository.findById(id)
                .map(document -> {
                    document.setName(updatedDocument.getName());
                    document.setUrl(updatedDocument.getUrl());
                    document.setIconName(updatedDocument.getIconName());
                    return documentRepository.save(document);
                })
                .orElseThrow(() -> new RuntimeException("Enlace no encontrado con ID: " + id));
    }

    @Override
    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Enlace no encontrado con ID: " + id);
        }
    }
}
