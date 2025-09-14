package com.teclab.practicas.WikiBackend.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MenuItemResponseDto {
        private Long id;
        private String name;
        private String path;
        private String icon;
        private Integer Order;
        private List<MenuItemResponseDto> children;     // Para la jerarquía de submenús
        private Long parentId;
}