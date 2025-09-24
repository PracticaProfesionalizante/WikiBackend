package com.teclab.practicas.WikiBackend;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.manu.MenuItemConverter;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemRequestDto;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemResponseDto;
import com.teclab.practicas.WikiBackend.entity.MenuItem;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.exception.MenuPathExistente;
import com.teclab.practicas.WikiBackend.repository.MenuItemRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.service.MenuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private MenuItemConverter menuItemConverter;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private MenuServiceImpl menuService;

    // Aquí irán los objetos de prueba (stubs)
    private MenuItem menuItem;
    private MenuItemResponseDto menuItemResponseDto;
    private MenuItemRequestDto menuItemRequestDto;
    private Roles adminRole;
    private Set<String> roleNames;

    @BeforeEach
    void setUp() {
        // Inicialización de objetos de prueba antes de cada test
        adminRole = new Roles();
        adminRole.setId(1L);
        adminRole.setName(Roles.RoleName.ROLE_ADMIN);

        roleNames = new HashSet<>();
        roleNames.add("ADMIN");

        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Dashboard");
        menuItem.setPath("/dashboard");
        menuItem.setRoles(Collections.singleton(adminRole));

        menuItemResponseDto = new MenuItemResponseDto();
        menuItemResponseDto.setId(1L);
        menuItemResponseDto.setName("Dashboard");
        menuItemResponseDto.setPath("/dashboard");

        menuItemRequestDto = new MenuItemRequestDto();
        menuItemRequestDto.setName("Dashboard");
        menuItemRequestDto.setPath("/dashboard");
        menuItemRequestDto.setRoles(roleNames);
    }

    @Test
    void getMenuByRoles_ShouldReturnMenuItems_WhenRolesAreValid() {
        // Arrange
        String jwtToken = "valid-jwt-token";
        Set<String> rolesFromToken = new HashSet<>(Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
        List<MenuItem> mockMenuItems = Arrays.asList(menuItem);

        when(jwtUtils.getRolesFromToken(jwtToken)).thenReturn(rolesFromToken);
        when(menuItemRepository.findMainMenusByRoles(rolesFromToken)).thenReturn(mockMenuItems);
        when(menuItemConverter.toDto(any(MenuItem.class))).thenReturn(menuItemResponseDto);

        // Act
        List<MenuItemResponseDto> result = menuService.getMenuByRoles(jwtToken);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(jwtUtils).getRolesFromToken(jwtToken);
        verify(menuItemRepository).findMainMenusByRoles(rolesFromToken);
        verify(menuItemConverter, times(mockMenuItems.size())).toDto(any(MenuItem.class));
    }

    @Test
    void getMenuByRoles_ShouldThrowException_WhenJwtIsInvalid() {
        // Arrange
        String invalidJwt = "invalid-jwt";
        doThrow(new IllegalArgumentException("Invalid JWT")).when(jwtUtils).getRolesFromToken(invalidJwt);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> menuService.getMenuByRoles(invalidJwt));
        verify(jwtUtils).getRolesFromToken(invalidJwt);
        verifyNoInteractions(menuItemRepository); // Aseguramos que el repositorio no fue llamado
    }

    @Test
    void createMenuItem_ShouldCreateAndReturnNewMenuItem() {
        // Arrange
        MenuItem createdItem = new MenuItem();
        createdItem.setId(2L);
        createdItem.setName(menuItemRequestDto.getName());
        createdItem.setPath(menuItemRequestDto.getPath());
        createdItem.setOrder(1);

        when(menuItemConverter.toEntity(any(MenuItemRequestDto.class), eq(1), isNull(), anySet())).thenReturn(menuItem);
        when(menuItemRepository.findMaxOrderByParentId(any())).thenReturn(Optional.of(0));
        when(rolesRepository.findByName(any())).thenReturn(Optional.of(adminRole));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(createdItem);
        when(menuItemConverter.toDto(any(MenuItem.class))).thenReturn(menuItemResponseDto);

        // Act
        MenuItemResponseDto result = menuService.createMenuItem(menuItemRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(menuItemResponseDto.getName(), result.getName());
        verify(menuItemRepository).findMaxOrderByParentId(null);
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void createMenuItem_ShouldThrowMenuPathExistente_WhenPathAlreadyExists() {
        // Arrange
        MenuItem newItem = new MenuItem();
        newItem.setPath("/existing-path");
        when(menuItemConverter.toEntity(any(MenuItemRequestDto.class), eq(1), isNull(), anySet())).thenReturn(newItem);
        when(menuItemRepository.findMaxOrderByParentId(any())).thenReturn(Optional.of(0));
        when(rolesRepository.findByName(any())).thenReturn(Optional.of(adminRole));
        doThrow(new DataIntegrityViolationException("Path '"+ newItem.getPath() +"' already exists")).when(menuItemRepository).save(any(MenuItem.class));

        // Act & Assert
        assertThrows(MenuPathExistente.class, () -> menuService.createMenuItem(menuItemRequestDto));
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void deleteMenuItem_ShouldDeleteMenuItem_WhenItHasNoChildren() {
        // Arrange
        menuItem.setChildren(Collections.emptyList());
        menuItem.setParent(null);
        menuItem.setOrder(1);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        // Act
        menuService.deleteMenuItem(1L);

        // Assert
        verify(menuItemRepository).findById(1L);
        verify(menuItemRepository).deleteById(1L);
        verify(menuItemRepository).adjustOrderOnDeleteForParentedItems(null, menuItem.getOrder());
    }

    @Test
    void deleteMenuItem_ShouldThrowException_WhenMenuItemHasChildren() {
        // Arrange
        MenuItem childItem = new MenuItem();
        menuItem.setChildren(Collections.singletonList(childItem)); // Asignar un hijo

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> menuService.deleteMenuItem(1L));
        verify(menuItemRepository).findById(1L);
        verify(menuItemRepository, never()).deleteById(anyLong()); // Verificar que deleteById no fue llamado
    }
}