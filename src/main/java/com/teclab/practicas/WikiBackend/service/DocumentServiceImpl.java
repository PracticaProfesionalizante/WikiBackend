package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.converter.document.DocumentConverter;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentTextRequestDTO;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentTextResponseDTO;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentUrlRequestDto;
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

    @Override
    public DocumentDetailResponseDto createDocument(DocumentUrlRequestDto requestDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Set<Roles> roles = setRoles(requestDto.getRoles());

            Document newDocument = documentConverter.dtoToUrlEntity(
                    requestDto,
                    roles,
                    currentUsername,
                    currentUsername
            );

            Document savedDocument = documentRepository.save(newDocument);
            return documentConverter.entityToUrlDetailResponse(savedDocument);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public List<DocumentDetailResponseDto> getAllDocuments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        List<Document> documents;
        if (userRoles.contains("ROLE_SUPER_USER")) documents = documentRepository.findAll();
        else documents = documentRepository.findDocumentsByRole(userRoles);

        return documents.stream()
                .map(documentConverter::entityToUrlDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDetailResponseDto getDocumentByRoles(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));
        return documentConverter.entityToUrlDetailResponse(document);
    }

    @Override
    public DocumentDetailResponseDto updateDocument(Long id, DocumentUrlRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        if (requestDto.getName() != null && !requestDto.getName().isBlank()) document.setName(requestDto.getName());
        if (requestDto.getIcon() != null && !requestDto.getIcon().isBlank()) document.setIconName(requestDto.getIcon());
        if (requestDto.getPath() != null && !requestDto.getPath().isBlank()) document.setPath(requestDto.getPath());
        if (requestDto.getRoles() != null && !requestDto.getRoles().isEmpty()) document.setRoles(setRoles(requestDto.getRoles()));
        document.setUpdatedBy(currentUsername);

        Document savedDocument = documentRepository.save(document);
        return documentConverter.entityToUrlDetailResponse(savedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Documento no encontrado con ID: " + id);
        }
    }




    @Override
    public DocumentTextResponseDTO createText(DocumentTextRequestDTO documentTextRequestDTO) {
        return null;
    }

    @Override
    public DocumentTextResponseDTO updateText(Long id, DocumentTextRequestDTO documentTextRequestDTO) {
        return null;
    }

    @Override
    public void deleteText(Long id) {

    }

    @Override
    public List<DocumentTextResponseDTO> getAllTextDocuments() {
        return List.of();
    }

    @Override
    public DocumentTextResponseDTO getTextDocument(Long id) {
        return null;
    }


    private Set<Roles> setRoles(Set<String> rolesName){
        if (rolesName == null || rolesName.isEmpty()) throw new IllegalArgumentException("Debe ingresar al menos un rol");

        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf("ROLE_" + roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role no encontrado con el siguiente nombre: " + roleName)))
                .collect(Collectors.toSet());
    }
}
