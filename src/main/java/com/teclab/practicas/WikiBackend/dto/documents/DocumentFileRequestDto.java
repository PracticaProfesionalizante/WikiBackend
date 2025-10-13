package com.teclab.practicas.WikiBackend.dto.documents;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFileRequestDto {
    private String name;
    private String type;
    private String slug;
    private Boolean status;

    @NotNull(message = "El archivo a subir (file) es obligatorio.")
    private MultipartFile file;

    private String icon;
    private Set<String> roles;
}