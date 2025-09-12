package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.manu.MenuItemConverter;
import com.teclab.practicas.WikiBackend.entity.MenuItem;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.repository.MenuItemRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RolesRepository rolesRepository;
    private final JwtUtils jwtUtils;

    @Autowired
    public MenuServiceImpl(
            MenuItemRepository menuItemRepository,
            JwtUtils jwtUtils,
            RolesRepository rolesRepository) {
        this.menuItemRepository = menuItemRepository;
        this.jwtUtils = jwtUtils;
        this.rolesRepository = rolesRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDto> getMenuByRoles(String jwt) {
        try {
            Set<String> roleNames = jwtUtils.getRolesFromToken(jwt);
            return menuItemRepository.findMainMenusByRoles(roleNames).stream().map(
                    menuItem -> MenuItemConverter.toDto(menuItem)
            ).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("MenuService / getDynamicMenuByRoles - " + e.getMessage());
            throw e;
        }
    }

    @Transactional()
    public MenuItemResponseDto addMenuItem(MenuItemRequestDto request) {
        try {
            MenuItem newItem = MenuItemConverter.toEntity(request);
            newItem.setParent(setParent(request.getParentId()));
            newItem.setRoles(setRoles(request.getRoles()));

            return menuItemRepository.save(menuItem);

            return MenuItemResponseDto.toDto(menuItemRepository.save(newItem));
        } catch (Exception e) {
            System.out.println("MenuService / getDynamicMenuByRoles - " + e.getMessage());
            throw e;
        }
    }
    @Transactional
    private MenuItem setParent(Long parentId){
        if (parentId != null) {
            MenuItem parent = menuItemRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontro MenuItem con el siguiente id : " + parentId));
            return parent;
        }
        return null;
    }
    @Transactional
    private Set<Roles> setRoles(List<String> rolesName){
        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf(roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName)))
                .collect(Collectors.toSet());
    }

    @Transactional()
    public MenuItemResponseDto editMenuItem(Long id, MenuItemRequestDto request) {
        try {
            if (id == null) throw new IllegalArgumentException("No se identifica el item");

            MenuItem item = menuItemRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No se encuentra item con el id: " + id));

            MenuItem newItem = setNewItem(request, item);

            return menuItemRepository.save(menuItem);

            return MenuItemResponseDto.toDto(menuItemRepository.save(newItem));
        } catch (Exception e) {
            System.out.println("MenuService / getDynamicMenuByRoles - " + e.getMessage());
            throw e;
        }
    }
    private MenuItem setNewItem (MenuItemRequestDto dto, MenuItem item){
        if(request.getId() != null) item.setId(request.getId());
        if(request.getName() != null) item.setName(request.getName());
        if(request.getPath() != null) item.setPath(request.getPath());
        if(request.getIcon() != null) item.setIcon(request.getIcon());
        if(request.getOrder() != null) item.setOrder(request.getOrder());
        if(request.getParent() != null) item.setParent(setParent(request.getParentId()));
        if(request.getRoles() != null) item.setRoles(setRoles(request.getRoles()));

        return item;
    }

    @Transactional()
    public void deleteMenuItem(Long id) {
        try {
            if (id == null) throw new IllegalArgumentException("No se identifica el item");

            menuItemRepository.deleteById(id);
        } catch (Exception e) {
            System.out.println("MenuService / getDynamicMenuByRoles - " + e.getMessage());
            throw e;
        }
    }
}