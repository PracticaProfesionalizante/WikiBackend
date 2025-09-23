package com.teclab.practicas.WikiBackend.dto.documents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTextRequestDTO {

    @NotBlank(message = "El título no puede estar en blanco.")
    @Size(min = 3, max = 255, message = "El título debe tener entre 3 y 255 caracteres.")
    private String title;

    @NotNull(message = "El contenido no puede ser nulo.")
    @Size(max = 10485760, message = "El contenido del documento no puede exceder los 10MB.")
    private String content;

    private Set<String> roles;
}