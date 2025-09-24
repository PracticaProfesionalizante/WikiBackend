package com.teclab.practicas.WikiBackend.converter.document;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentUrlRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.entity.Roles;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DocumentConverter {

    public Document dtoToUrlEntity(
            DocumentUrlRequestDto dto,
            Set<Roles> roles,
            String createBy,
            String updateBy
    ) {
        Document document = new Document();
        document.setName(dto.getName());
        document.setType(Document.TypeName.TYPE_URL);
        document.setPath(dto.getPath());
        document.setIconName(dto.getIcon());
        document.setRoles(roles);
        document.setCreatedBy(createBy);
        document.setUpdatedBy(updateBy);

        return document;
    }

    public DocumentDetailResponseDto entityToUrlDetailResponse(Document document) {
        DocumentDetailResponseDto dto = new DocumentDetailResponseDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setType(document.getType().toString());
        dto.setPath(document.getPath());
        dto.setIcon(document.getIconName());
        dto.setIcon(document.getIconName());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setRoles(document.getRoles().stream().map(roles -> roles.getName().name()).collect(Collectors.toSet()));
        return dto;
    }

}