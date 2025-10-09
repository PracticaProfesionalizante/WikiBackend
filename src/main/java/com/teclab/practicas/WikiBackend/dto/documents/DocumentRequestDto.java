package com.teclab.practicas.WikiBackend.dto.documents;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequestDto {
    private String name;
    private String type;
    private String slug;
    @Size(max = 10485760, message = "El contenido del documento no puede exceder los 10MB.")
    private String content;
    private String icon;
    private Set<String> roles;
}