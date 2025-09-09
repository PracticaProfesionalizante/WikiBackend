package com.teclab.practicas.WikiBackend;

import com.teclab.practicas.WikiBackend.converter.auth.RegisterConverter;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterRequestDto;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.repository.UserRepository;
import com.teclab.practicas.WikiBackend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RolesRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterConverter registerConverter;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequestDto userRegisterRequestDto;
    private RegisterResponseDto userRegisterResponseDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        Set<String> rolesString = Set.of("SUPER_USER", "ADMIN", "COLLABORATOR");
        Set<Roles> rolesRoles = Set.of(
            getRole(Roles.RoleName.ROLE_SUPER_USER),
            getRole(Roles.RoleName.ROLE_ADMIN),
            getRole(Roles.RoleName.ROLE_COLLABORATOR)
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("username");
        testUser.setEmail("test@example.com");
        testUser.setPassword("passwordEncriptada");
        testUser.setRoles(rolesRoles);

        userRegisterRequestDto = new RegisterRequestDto();
        userRegisterRequestDto.setUsername("username");
        userRegisterRequestDto.setEmail("test@example.com");
        userRegisterRequestDto.setPassword("Test123*");
        userRegisterRequestDto.setRoles(rolesString);

        userRegisterResponseDto = new RegisterResponseDto();
        userRegisterResponseDto.setId(1L);
        userRegisterResponseDto.setUsername("username");
        userRegisterResponseDto.setEmail("test@example.com");
        userRegisterResponseDto.setEnabled(true);
        userRegisterResponseDto.setCreatedAt(LocalDateTime.now());
        userRegisterResponseDto.setUpdatedAt(LocalDateTime.now());
        userRegisterResponseDto.setRoles(Set.of("SUPER_USER", "ADMIN", "COLLABORATOR"));
    }
    private Roles getRole(Roles.RoleName name){
        Roles rol = new Roles();
        rol.setName(name);
        if (name == Roles.RoleName.ROLE_SUPER_USER) rol.setId(1L);
        if (name == Roles.RoleName.ROLE_ADMIN) rol.setId(2L);
        if (name == Roles.RoleName.ROLE_COLLABORATOR) rol.setId(3L);
        return rol;
    }

    @Test
    void testCreateUser_shouldReturnSavedUser() {
        //GIVEN
        when(registerConverter.toEntity(any(RegisterRequestDto.class))).thenReturn(testUser);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("passwordEncriptada");
        when(roleRepository.findByName(Roles.RoleName.ROLE_SUPER_USER)).thenReturn(Optional.of(getRole(Roles.RoleName.ROLE_SUPER_USER)));
        when(roleRepository.findByName(Roles.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(getRole(Roles.RoleName.ROLE_ADMIN)));
        when(roleRepository.findByName(Roles.RoleName.ROLE_COLLABORATOR)).thenReturn(Optional.of(getRole(Roles.RoleName.ROLE_COLLABORATOR)));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(registerConverter.toDto(any(User.class))).thenReturn(userRegisterResponseDto);

        // WHEN
        RegisterResponseDto createdUser = userService.createUser(userRegisterRequestDto);

        // THEN
        assertNotNull(createdUser);
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
//
//    @Test
//    void testFindUserById_shouldReturnUser_whenUserExists() {
//        // 1. Preparación (Given):
//        // Se simula que findById() devuelva un usuario existente.
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//
//        // 2. Acción (When):
//        // Se llama al método del servicio.
//        Optional<User> foundUser = userService.findUserById(1L);
//
//        // 3. Verificación (Then):
//        // Se asegura de que el usuario fue encontrado.
//        assertTrue(foundUser.isPresent());
//        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
//    }
//
//    @Test
//    void testFindUserById_shouldReturnEmpty_whenUserDoesNotExist() {
//        // 1. Preparación (Given):
//        // Se simula que findById() devuelve un Optional vacío.
//        when(userRepository.findById(2L)).thenReturn(Optional.empty());
//
//        // 2. Acción (When):
//        Optional<User> foundUser = userService.findUserById(2L);
//
//        // 3. Verificación (Then):
//        // Se asegura de que el Optional está vacío.
//        assertFalse(foundUser.isPresent());
//    }
}