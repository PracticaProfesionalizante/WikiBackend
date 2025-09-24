package com.teclab.practicas.WikiBackend.converter.document;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.entity.Roles;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DocumentConverter {

    public Document toEntity(
            DocumentRequestDto dto,
            Document.TypeName type,
            Set<Roles> roles,
            String createBy,
            String updateBy
    ) {
        Document document = new Document();
        document.setName(dto.getName());
        document.setType(type);
        document.setContent(dto.getContent());
        document.setIconName(dto.getIcon());
        document.setRoles(roles);
        document.setCreatedBy(createBy);
        document.setUpdatedBy(updateBy);

        return document;
    }

    public DocumentDetailResponseDto toDetailResponse(Document document) {
        DocumentDetailResponseDto dto = new DocumentDetailResponseDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setType(document.getType().toString());
        dto.setContent(document.getContent());
        dto.setIcon(document.getIconName());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setRoles(document.getRoles().stream().map(roles -> roles.getName().name()).collect(Collectors.toSet()));
        return dto;
    }

    public DocumentDetailResponseDto toSummaryResponse(Document document) {
        DocumentDetailResponseDto dto = new DocumentDetailResponseDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setType(document.getType().toString());
        dto.setIcon(document.getIconName());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setRoles(document.getRoles().stream().map(roles -> roles.getName().name()).collect(Collectors.toSet()));
        return dto;
    }

}