package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentUrlRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private final DocumentService documentService;

    /**
     * Endpoint para crear un documento de tipo URL.
     * Solo accesible para SuperUser y Admin.
     */
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    @PostMapping("/url")
    public ResponseEntity<DocumentDetailResponseDto> createUrlDocument(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody DocumentUrlRequestDto request
    ) {
        String token = authorizationHeader.substring(7);
        DocumentDetailResponseDto documentId = documentService.createDocument(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentId);
    }

    /**
     * Endpoint para editar un documento de tipo URL.
     * Solo accesible para SuperUser y Admin.
     */
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    @PutMapping("/url/{id}")
    public ResponseEntity<DocumentDetailResponseDto> editUrlDocument(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Long id,
            @RequestBody DocumentUrlRequestDto request
    ) {
        String token = authorizationHeader.substring(7);
        DocumentDetailResponseDto documentId = documentService.updateDocument(token, id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentId);
    }

    /**
     * Endpoint para eliminar un documento de tipo URL.
     * Solo accesible para SuperUser y Admin.
     */
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<DocumentDetailResponseDto> deleteUrlDocument(
            @PathVariable Long id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para obtener un documento por ID.
     * Accesible para todos los roles (Lectura).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponseDto> getDocument(@PathVariable Long id) {
        DocumentDetailResponseDto documentContent = documentService.getDocumentByRoles(id);
        return ResponseEntity.ok(documentContent);
    }

    /**
     * Endpoint para obtener todos los Documentos.
     * Accesible para todos los roles (Lectura).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/urls")
    public ResponseEntity<List<DocumentDetailResponseDto>> getAllDocument() {
        List<DocumentDetailResponseDto> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }
}