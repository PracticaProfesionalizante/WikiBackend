package com.teclab.practicas.WikiBackend;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.repository.UserRepository;
import com.teclab.practicas.WikiBackend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito para JUnit
public class UserServiceTest {

    @Mock // Crea un mock del UserRepository
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks // Inyecta los mocks en UserService
    private UserServiceImpl userServiceImpl;

    private User testUser1;
    private User testUser2;

    @BeforeEach
        // Se ejecuta antes de cada test
    void setUp() {

        // Inicializar usuarios de prueba
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUsername("testuser1");
        testUser1.setEmail("prueba1@gmail.com");
        testUser1.setEnabled(true);
        testUser1.setCreatedAt(LocalDateTime.now().minusDays(5));
        testUser1.setUpdatedAt(LocalDateTime.now().minusDays(1));
        testUser1.setPassword("contraseña1");
        testUser1.setRoles(new HashSet<>(List.of(new Roles(1L, Roles.RoleName.ROLE_COLLABORATOR, null, null,null))));

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setEmail("prueba2@gmail.com");
        testUser2.setEnabled(true);
        testUser2.setCreatedAt(LocalDateTime.now().minusDays(10));
        testUser2.setUpdatedAt(LocalDateTime.now().minusDays(2));
        testUser2.setPassword("contraseña2");
        testUser2.setRoles(new HashSet<>(Arrays.asList(new Roles(2L, Roles.RoleName.ROLE_SUPER_USER, null, null,null))));
    }

    @Test
    void testGetAllUsers() {
        // Simular el comportamiento del repositorio
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser1, testUser2));

        // Llamar al metodo del servicio
        List<UserResponseDto> result = userServiceImpl.getAllUsers();

        // Verificar el resultado
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser1", result.get(0).getUsername());
        assertTrue(result.get(0).getRoles().contains("ROLE_COLLABORATOR"));
        assertEquals("testuser2", result.get(1).getUsername());
        assertTrue(result.get(1).getRoles().contains("ROLE_SUPER_USER"));

        // Verificar que el metodo del repositorio fue llamado
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetMyUser() {
        // Simular el comportamiento del repositorio
        when(userRepository.findByEmail(testUser1.getEmail())).thenReturn(Optional.of(testUser1));
        when(jwtUtils.getUsername(any(String.class))).thenReturn("prueba1@gmail.com");

        // Llamar al metodo del servicio
        UserResponseDto result = userServiceImpl.getMyUser("testuser1");

        // Verificar el resultado
        assertNotNull(result);
        assertEquals("testuser1", result.getUsername());
        assertEquals("prueba1@gmail.com", result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_COLLABORATOR"));

        // Verificar que el metodo del repositorio fue llamado
        verify(userRepository, times(1)).findByEmail(testUser1.getEmail());
    }

    @Test
    void testGetMyUserNotFound() {
        // Simular el comportamiento del repositorio cuando no encuentra el usuario
        when(userRepository.findByEmail(testUser1.getEmail())).thenThrow(new UsernameNotFoundException("No se encontró ningún usuario con ese email"));
        when(jwtUtils.getUsername(any(String.class))).thenReturn("prueba1@gmail.com");

        // Verificar que se lanza la excepción correcta
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userServiceImpl.getMyUser("nonexistent");
        });
        assertEquals("No se encontró ningún usuario con ese email", thrown.getMessage());

        // Verificar que el metodo del repositorio fue llamado
        verify(userRepository, times(1)).findByEmail(testUser1.getEmail());
    }
}