package com.teclab.practicas.WikiBackend.converter.document;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentFileRequestDto;
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
            Set<Roles> roles,
            String createBy,
            String updateBy
    ) {
        if (dto == null) throw new IllegalArgumentException("La request no puede ser vacia");;
        if( dto.getName() == null || dto.getName().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( dto.getType() == null || dto.getType().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        Document.TypeName type = null;
        try {
            type = Document.TypeName.valueOf(dto.getType());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Tipo de archivo invalido");
        }
        if( dto.getSlug() == null || dto.getSlug().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( dto.getContent() == null || dto.getContent().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( dto.getIcon() == null || dto.getIcon().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( roles == null || roles.isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( createBy == null || createBy.isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( updateBy == null || updateBy.isEmpty() ) throw new IllegalArgumentException("Falta un campo");

        Document document = new Document();
        document.setName(dto.getName());
        document.setType(type);
        document.setSlug(dto.getSlug());
        document.setContent(dto.getContent());
        document.setIconName(dto.getIcon());
        document.setRoles(roles);
        document.setCreatedBy(createBy);
        document.setUpdatedBy(updateBy);

        return document;
    }
    public Document toEntity(
            DocumentFileRequestDto dto,
            Set<Roles> roles,
            String createBy,
            String updateBy
    ) {
        if (dto == null) throw new IllegalArgumentException("La request no puede ser vacia");;
        if( dto.getName() == null || dto.getName().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( dto.getType() == null || dto.getType().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        Document.TypeName type = null;
        try {
            type = Document.TypeName.valueOf(dto.getType());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Tipo de archivo invalido");
        }
        if( dto.getSlug() == null || dto.getSlug().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( dto.getIcon() == null || dto.getIcon().isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( roles == null || roles.isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( createBy == null || createBy.isEmpty() ) throw new IllegalArgumentException("Falta un campo");
        if( updateBy == null || updateBy.isEmpty() ) throw new IllegalArgumentException("Falta un campo");

        Document document = new Document();
        document.setName(dto.getName());
        document.setType(type);
        document.setSlug(dto.getSlug());
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
        dto.setSlug(document.getSlug());
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
        dto.setSlug(document.getSlug());
        dto.setIcon(document.getIconName());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setRoles(document.getRoles().stream().map(roles -> roles.getName().name()).collect(Collectors.toSet()));
        return dto;
    }

}