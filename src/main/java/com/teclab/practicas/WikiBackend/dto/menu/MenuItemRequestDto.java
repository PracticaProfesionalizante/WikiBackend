package com.teclab.practicas.WikiBackend.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MenuItemRequestDto {

    @NotBlank
    private String name;
    @NotBlank
    private String path;
    @NotBlank
    private String icon;
    @NotBlank @NotNull
    private Integer displayOrder;
    @NotBlank
    private Long parent;
        
        
}