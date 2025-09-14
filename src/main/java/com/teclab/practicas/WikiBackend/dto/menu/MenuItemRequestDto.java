package com.teclab.practicas.WikiBackend.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequestDto {
//    @NotBlank
    private String name;
//    @NotBlank
    private String path;
//    @NotBlank
    private String icon;
//    @NotBlank @NotNull
    private Integer order;
//    @NotBlank
    private Long parentId;
//    @NotEmpty(message = "La lista de roles no puede estar vacía.")
    private Set<String> roles;
}