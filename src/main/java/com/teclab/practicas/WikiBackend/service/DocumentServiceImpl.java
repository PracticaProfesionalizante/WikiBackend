package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentUrlRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.repository.DocumentRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.catalina.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final RolesRepository rolesRepository;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository, RolesRepository rolesRepository) {
        this.documentRepository = documentRepository;
        this.rolesRepository = rolesRepository;
    }
    private DocumentDetailResponseDto mapToDto(Document document) {
        DocumentDetailResponseDto dto = new DocumentDetailResponseDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setType(document.getType());
        dto.setPath(document.getPath());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());
        dto.setAccessRoles(document.getRoles().stream()
                .map(Roles::getName)
                .collect(Collectors.toSet()));
        return dto;
    }

    private Document mapToEntity(DocumentUrlRequestDto dto) {
        Document document = new Document();
        document.setName(dto.getName());
        document.setType(dto.getType());
        document.setPath(dto.getPath());

        if (dto.getAccessRoles() != null && !dto.getAccessRoles().isEmpty()) {
            Set<Roles> roles = setRoles(dto.getRoles());
            document.setRoles(roles);
        }
        return document;
    }

    @Override
    public DocumentDetailResponseDto createDocument(DocumentUrlRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Document newDocument = mapToEntity(requestDto);
        newDocument.setCreatedBy(currentUsername);

        Document savedDocument = documentRepository.save(newDocument);
        return mapToDto(savedDocument);
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

        if (userRoles.contains("ROLE_SUPER_USER")) {
            // SuperUser puede ver todos los documentos
            List<Document> documents = documentRepository.findAll();
            return documents.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } else {
            // Los demás roles solo pueden ver los documentos a los que tienen acceso
            List<Document> documents = documentRepository.findByAccessRoles_NameIn(userRoles);
            return documents.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public DocumentDetailResponseDto getDocumentByRoles(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));
        return mapToDto(document);
    }

    @Override
    public DocumentDetailResponseDto updateDocument(Long id, DocumentUrlRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        document.setName(requestDto.getName());
        document.setType(requestDto.getType());
        document.setPath(requestDto.getPath());
        document.setUpdatedBy(currentUsername);

        if (requestDto.getAccessRoles() != null && !requestDto.getAccessRoles().isEmpty()) {
            Set<Roles> roles = setRoles(requestDto.getRoles());
            document.setRoles(roles);
        } else {
            document.setRoles(Collections.emptySet());
        }

        Document updatedDocument = documentRepository.save(document);
        return mapToDto(updatedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Documento no encontrado con ID: " + id);
        }
    }
    private Set<Roles> setRoles(Set<String> rolesName){
        if (rolesName == null || rolesName.isEmpty()) throw new IllegalArgumentException("Debe ingresar al menos un rol");

        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf("ROLE_" + roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role no encontrado con el siguiente nombre: " + roleName)))
                .collect(Collectors.toSet());
    }
}
