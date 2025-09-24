package com.teclab.practicas.WikiBackend.dto.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUrlRequestDto {
    private String name;
    private String path;
    private String icon;
    private Set<String> roles;
}