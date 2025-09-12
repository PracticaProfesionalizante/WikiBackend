package com.teclab.practicas.WikiBackend.service;

import java.util.List;

public interface MenuService {
    public List<MenuItemResponseDto> getMenuByRoles(String jwt);
    public MenuItemResponseDto addMenuItem(MenuItemRequestDto request);
    public MenuItemResponseDto editMenuItem(Long id, MenuItemRequestDto request);
    public void deleteMenuItem(Long id);
}
