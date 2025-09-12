package com.teclab.practicas.WikiBackend.converter.manu;

import com.teclab.practicas.WikiBackend.entity.MenuItem;
import org.springframework.stereotype.Component;

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

    public MenuItem toEntity(MenuItemRequestDto dto) {
        if (dto == null) return null;
        if (dto.getName() == null || dto.getUsername().isEmpty())  return null;
        if (dto.getPath() == null || dto.getPath().isEmpty())  return null;
        if (dto.getIcon() == null || dto.getIcon().isEmpty())  return null;
        if (dto.getOrder() == null || dto.getOrder() != 0)  return null;

        MenuItem item = new MenuItem();
        item.setName(dto.getName());
        item.setPath(dto.getPath());
        item.setIcon(dto.getIcon());
        item.setOrder(dto.getOrder());

        return item;
    }

    public MenuItemResponseDto toDto(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }

        MenuItemResponseDto item = new MenuItemResponseDto();
        item.getId(menuItem.getId());
        item.setName(menuItem.getName());
        item.setPath(menuItem.getPath());
        item.setIcon(menuItem.getIcon());
        item.setOrder(menuItem.getOrder());
        item.setParent(menuItem.getParent());
        item.getChildren(menuItem.getChildren());

        // Mapeamos el Set<Roles> a un Set<String> con los nombres de los roles
        if (menuItem.getRoles() != null) {
            item.setRoles(menuItem.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toSet()));
        }

        return item;
    }
}