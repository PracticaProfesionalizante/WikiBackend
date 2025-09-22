package com.teclab.practicas.WikiBackend.dto.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDetailResponseDto {
    private Long id;
    private String name;
    private String type;
    private String path;
    private LocalDateTime updatedAt;
}