package com.teclab.practicas.WikiBackend.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequestDto {
    private String name;
    private String path;
    private String icon;
    private String view;
    private Integer order;
    private Long parentId;
    private Set<String> roles;
}