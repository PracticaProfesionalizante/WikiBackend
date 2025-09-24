package com.teclab.practicas.WikiBackend.dto.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDetailResponseDto {
    private Long id;
    private String name;
    private String type;
    private String path;
    private String icon;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Set<String> roles;
}