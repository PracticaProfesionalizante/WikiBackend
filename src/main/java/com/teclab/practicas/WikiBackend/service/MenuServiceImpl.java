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
    private MenuItem setParent(Long parentId){
        if (parentId != null) {
            MenuItem parent = menuItemRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontro MenuItem con el siguiente id : " + parentId));
            return parent;
        }
        return null;
    }
    private Set<Roles> setRoles(List<String> rolesName){
        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf(roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName)))
                .collect(Collectors.toSet());
    }

    @Transactional()
    public MenuItemResponseDto editMenuItem(Long id, MenuItemRequestDto request) {

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró MenuItem con el id: " + id));

        Integer oldOrder = menuItem.getOrder();
        Integer newOrder = request.getOrder();

        Long menuItemParentId = (menuItem.getParent() != null) ? menuItem.getParent().getId() : null;
        Long requestParentId = request.getParentId();
        if (menuItemParentId == null && requestParentId != null ||
                menuItemParentId != null && requestParentId == null ||
                (menuItemParentId != null && requestParentId != null && !menuItemParentId.equals(requestParentId))) {
            menuItemRepository.adjustOrderOnDelete(menuItem.getParent().getId(), oldOrder);
            menuItemRepository.adjustOrderOnChangeParent(request.getParentId(), newOrder);
            menuItem.setParent(setParent(requestParentId));
        } else if (newOrder != null && !newOrder.equals(oldOrder)) {
            adjustItemOrder(menuItem.getParent().getId(), oldOrder, newOrder);
            menuItem.setOrder(newOrder);
        }

        if (request.getName() != null) menuItem.setName(request.getName());
        if (request.getPath() != null) menuItem.setPath(request.getPath());
        if (request.getIcon() != null) menuItem.setIcon(request.getIcon());
        if (request.getRoles() != null) menuItem.setRoles(setRoles(request.getRoles()));

        return MenuItemResponseDto.toDto(menuItemRepository.save(menuItem));
    }
    private void adjustItemOrder(Long parentId, Integer oldOrder, Integer newOrder) {
        if (oldOrder < newOrder) {
            menuItemRepository.decreaseOrder(parentId, oldOrder + 1, newOrder);
        } else if (oldOrder > newOrder) {
            menuItemRepository.increaseOrder(parentId, newOrder, oldOrder - 1);
        }
    }

    @Transactional()
    public void deleteMenuItem(Long id) {
        try {
            MenuItem menuItem = menuItemRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró MenuItem con el id: " + id));

            // Obtener el orden del ítem que se va a eliminar
            Integer orderToDelete = menuItem.getOrder();

            // 1. Eliminar el ítem
            menuItemRepository.delete(menuItem);

            // 2. Reajustar el orden de los demás ítems
            // Se llama al metodo para decrementar el orden de todos los ítems que le seguían.
            menuItemRepository.adjustOrderOnDelete(menuItem.getParent().getId(), orderToDelete);
        } catch (Exception e) {
            System.out.println("MenuService / getDynamicMenuByRoles - " + e.getMessage());
            throw e;
        }
    }
}