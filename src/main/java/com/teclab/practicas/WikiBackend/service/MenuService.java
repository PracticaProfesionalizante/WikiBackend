package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.menu.MenuItemRequestDto;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemResponseDto;

import java.util.List;

public interface MenuService {
    public List<MenuItemResponseDto> getMenuByRoles(String jwt);
    public MenuItemResponseDto createMenuItem(MenuItemRequestDto request);
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto request);
    public void deleteMenuItem(Long id);
}
