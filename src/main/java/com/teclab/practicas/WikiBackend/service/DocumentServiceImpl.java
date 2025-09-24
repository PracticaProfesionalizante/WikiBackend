package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.converter.document.DocumentConverter;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.repository.DocumentRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final RolesRepository rolesRepository;
    private final DocumentConverter documentConverter;

    @Autowired
    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            DocumentConverter documentConverter,
            RolesRepository rolesRepository
    ) {
        this.documentRepository = documentRepository;
        this.documentConverter = documentConverter;
        this.rolesRepository = rolesRepository;
    }

    //----------------------- IMPLEMENTACION PARA TEXT Y URL -----------------------
    @Override
    public DocumentDetailResponseDto getDocumentByRoles(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));
        return documentConverter.toDetailResponse(document);
    }

    @Override
    public DocumentDetailResponseDto updateDocument(Long id, DocumentRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) document.setName(request.getName());
        if (request.getIcon() != null && !request.getIcon().isBlank()) document.setIconName(request.getIcon());
        if (request.getContent() != null && !request.getContent().isBlank()) document.setContent(request.getContent());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) document.setRoles(setRoles(request.getRoles()));
        document.setUpdatedBy(currentUsername);

        Document savedDocument = documentRepository.save(document);
        return documentConverter.toDetailResponse(savedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Documento no encontrado con ID: " + id);
        }
    }
    //-----------------------------------------------------------------------------

    //----------------------- IMPLEMENTACION PARA URL -----------------------
    @Override
    public DocumentDetailResponseDto createDocument(DocumentRequestDto request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Set<Roles> roles = setRoles(request.getRoles());

            Document newDocument = documentConverter.toEntity(
                    request,
                    Document.TypeName.TYPE_URL,
                    roles,
                    currentUsername,
                    currentUsername
            );

            Document savedDocument = documentRepository.save(newDocument);
            return documentConverter.toDetailResponse(savedDocument);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDetailResponseDto> getAllUrlDocuments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        List<Document> documents;
        if (userRoles.contains("ROLE_SUPER_USER")) documents = documentRepository.findByType(Document.TypeName.TYPE_URL);
        else documents = documentRepository.findDocumentsByRoleAndByType(userRoles, Document.TypeName.TYPE_URL);

        return documents.stream()
                .map(documentConverter::toDetailResponse)
                .collect(Collectors.toList());
    }
    //-----------------------------------------------------------------------------

    //----------------------- IMPLEMENTACION PARA TEXT -----------------------
    @Override
    public DocumentDetailResponseDto createText(DocumentRequestDto request){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Set<Roles> roles = setRoles(request.getRoles());

            Document newDocument = documentConverter.toEntity(
                    request,
                    Document.TypeName.TYPE_TEXT,
                    roles,
                    currentUsername,
                    currentUsername
            );

            Document savedDocument = documentRepository.save(newDocument);
            return documentConverter.toDetailResponse(savedDocument);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDetailResponseDto> getAllTextDocuments(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        List<Document> documents;
        if (userRoles.contains("ROLE_SUPER_USER")) documents = documentRepository.findByType(Document.TypeName.TYPE_TEXT);
        else documents = documentRepository.findDocumentsByRoleAndByType(userRoles, Document.TypeName.TYPE_TEXT);

        return documents.stream()
                .map(documentConverter::toSummaryResponse)
                .collect(Collectors.toList());
    }
    //-----------------------------------------------------------------------------



    private Set<Roles> setRoles(Set<String> rolesName){
        if (rolesName == null || rolesName.isEmpty()) throw new IllegalArgumentException("Debe ingresar al menos un rol");

        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf("ROLE_" + roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role no encontrado con el siguiente nombre: " + roleName)))
                .collect(Collectors.toSet());
    }
}
