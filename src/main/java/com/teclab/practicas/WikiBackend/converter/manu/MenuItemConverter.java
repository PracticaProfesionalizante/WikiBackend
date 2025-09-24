package com.teclab.practicas.WikiBackend.converter.manu;

import com.teclab.practicas.WikiBackend.dto.menu.MenuItemRequestDto;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemResponseDto;
import com.teclab.practicas.WikiBackend.entity.MenuItem;
import com.teclab.practicas.WikiBackend.entity.Roles;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MenuItemConverter {

    public MenuItem toEntity(Long id, MenuItemRequestDto dto) {
        if (dto == null) return null;
        if (id == null || id < 1)  return null;

        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName(dto.getName());
        item.setPath(dto.getPath());
        item.setIcon(dto.getIcon());
        item.setOrder(dto.getOrder());

        return item;
    }

    public MenuItem toEntity(
            MenuItemRequestDto dto,
            int newOrder,
            MenuItem parent,
            Set<Roles> roles
    ) {
        if (dto == null) throw new IllegalArgumentException("La request no puede ser vacia");
        System.out.println("dto - " + dto);

        if (dto.getName() == null || dto.getName().isBlank())   throw new IllegalArgumentException("Falta el campo Name");
        System.out.println("dto.getName() - " + dto.getName());

        if (dto.getPath() == null || dto.getPath().isBlank())   throw new IllegalArgumentException("Falta el campo Path");
        System.out.println("dto.getPath() - " + dto.getPath());

        if (dto.getIcon() == null || dto.getIcon().isBlank())   throw new IllegalArgumentException("Falta el campo Icon");
        System.out.println("dto.getIcon() - " + dto.getIcon());


        MenuItem item = new MenuItem();
        item.setName(dto.getName());
        item.setPath(dto.getPath());
        item.setIcon(dto.getIcon());
        item.setView(dto.getView());
        item.setOrder(newOrder);
        item.setParent(parent);
        item.setRoles(roles);

        return item;
    }

    public MenuItemResponseDto toDto(MenuItem menuItem) {
        if (menuItem == null) return null;

        MenuItemResponseDto dto = new MenuItemResponseDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setPath(menuItem.getPath());
        dto.setIcon(menuItem.getIcon());
        dto.setView(menuItem.getView());
        dto.setOrder(menuItem.getOrder());
        dto.setParentId((menuItem.getParent() != null) ? menuItem.getParent().getId() : null);

        // Mapear recursivamente los hijos a DTOs
        if (menuItem.getChildren() != null && !menuItem.getChildren().isEmpty()) {
            dto.setChildren(menuItem.getChildren().stream()
                    .map(this::toDto) // Mapeo recursivo
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}