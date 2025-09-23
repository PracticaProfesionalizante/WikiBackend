package com.teclab.practicas.WikiBackend.dto.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTextResponseDTO {
    private Long id;
    private String name;
    private String type;
    private String content;
    private LocalDateTime updatedAt;
}
