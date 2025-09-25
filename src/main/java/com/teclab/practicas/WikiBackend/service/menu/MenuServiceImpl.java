package com.teclab.practicas.WikiBackend.service.menu;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.manu.MenuItemConverter;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemRequestDto;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemResponseDto;
import com.teclab.practicas.WikiBackend.entity.MenuItem;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.exception.MenuPathExistente;
import com.teclab.practicas.WikiBackend.repository.MenuItemRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RolesRepository rolesRepository;
    private final MenuItemConverter menuItemConverter;
    private final JwtUtils jwtUtils;

    @Autowired
    public MenuServiceImpl(
            MenuItemRepository menuItemRepository,
            JwtUtils jwtUtils,
            MenuItemConverter menuItemConverter,
            RolesRepository rolesRepository) {
        this.menuItemRepository = menuItemRepository;
        this.jwtUtils = jwtUtils;
        this.menuItemConverter = menuItemConverter;
        this.rolesRepository = rolesRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDto> getMenuByRoles(String jwt) {
        try {
            Set<String> roleNames = jwtUtils.getRolesFromToken(jwt);
            return menuItemRepository.findMainMenusByRoles(roleNames).stream()
                    .map(menuItemConverter::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("getMenuByRoles - " + e.getMessage());
            throw e;
        }
    }

    @Transactional()
    public MenuItemResponseDto createMenuItem(MenuItemRequestDto request) {

        int newOrder = getMaxOrder(request.getParentId()) + 1;
        MenuItem parent = setParent(request.getParentId());
        Set<Roles> roles = setRoles(request.getRoles());

        MenuItem newItem = menuItemConverter.toEntity(
                request,
                newOrder,
                parent,
                roles
        );

        try {
            MenuItem itemCreated = menuItemRepository.save(newItem);
            return menuItemConverter.toDto(itemCreated);
        } catch (Exception e) {
            throw new MenuPathExistente("El path: '"+ newItem.getPath() +"' ya esta en uso");
        }
    }

    @Transactional()
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto request) {

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró MenuItem con el id: " + id));

        Long oldParentId = (menuItem.getParent() != null) ? menuItem.getParent().getId() : null;
        Long newParentId = request.getParentId();

        Integer oldOrder = menuItem.getOrder();
        Integer newOrder = request.getOrder();

        if (!Objects.equals(oldParentId, newParentId)){
            System.out.println("Adentro del if: oldParentId - " + oldParentId + "\t newParentId - " + newParentId);
            menuItemRepository.adjustOrderOnDeleteForParentedItems(oldParentId, oldOrder);

            Integer beforeValidate = newOrder;
            newOrder = getOrderValid(newParentId, newOrder);

            if (!Objects.equals(beforeValidate, newOrder)) newOrder++;

            menuItemRepository.adjustOrderOnChangeParent(newParentId, newOrder);
        } else {
            System.out.println("Adentro del else: oldParentId - " + oldParentId + "\t newParentId - " + newParentId);
            newOrder = getOrderValid(newParentId, newOrder);
            if (!newOrder.equals(oldOrder)) adjustItemOrder(newParentId, oldOrder, newOrder);

        }
        menuItem.setOrder(newOrder);

        menuItem.setParent(setParent(newParentId));

        if (request.getName() != null) menuItem.setName(request.getName());
        if (request.getPath() != null) menuItem.setPath(request.getPath());
        if (request.getIcon() != null) menuItem.setIcon(request.getIcon());
        menuItem.setView(request.getView());
        if (request.getRoles() != null) menuItem.setRoles(setRoles(request.getRoles()));

        return menuItemConverter.toDto(menuItemRepository.save(menuItem));
    }

    @Transactional()
    public void deleteMenuItem(Long id) {
        try {
            MenuItem menuItem = menuItemRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró MenuItem con el id: " + id));

            // Si el ítem a eliminar tiene hijos, es mejor lanzar una excepción
            // para evitar eliminaciones accidentales de sub-árboles completos
            if (menuItem.getChildren() != null && !menuItem.getChildren().isEmpty()) {
                throw new IllegalStateException("No se puede eliminar un menú que tiene submenús asociados.");
            }

            // Obtener el orden y el ID del padre (si existe)
            Integer orderToDelete = menuItem.getOrder();
            Long parentId = (menuItem.getParent() != null) ? menuItem.getParent().getId() : null;

            // 1. Eliminar el ítem. JpaRepository se encarga de esto de forma segura.
            menuItemRepository.deleteById(id);

            menuItemRepository.adjustOrderOnDeleteForParentedItems(parentId, orderToDelete);

        } catch (Exception e) {
            System.out.println("deleteMenuItem - " + e.getMessage());
            throw e;
        }
    }

    private Integer getOrderValid(Long parentId, Integer order){
        int maxOrder = getMaxOrder(parentId);
        if (maxOrder == 0) return 0;
        if (order != null && order <= maxOrder && order > 0) return order;
        else return maxOrder;
    }
    private int getMaxOrder(Long parentId){
        return menuItemRepository.findMaxOrderByParentId(parentId).orElse(0);
    }
    private void adjustItemOrder(Long parentId, Integer oldOrder, Integer newOrder) {
        if (oldOrder < newOrder) {
            menuItemRepository.decreaseOrder(parentId, oldOrder + 1, newOrder);
        } else if (oldOrder > newOrder) {
            menuItemRepository.increaseOrder(parentId, newOrder, oldOrder - 1);
        }
    }

    private MenuItem setParent(Long parentId){
        if (parentId != null) {
            return menuItemRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontro MenuItem con el siguiente id : " + parentId));
        }
        return null;
    }
    private Set<Roles> setRoles(Set<String> rolesName){
        if (rolesName == null || rolesName.isEmpty()) throw new IllegalArgumentException("Debe ingresar al menos un rol");

        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf("ROLE_" + roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role no encontrado con el siguiente nombre: " + roleName)))
                .collect(Collectors.toSet());
    }
}