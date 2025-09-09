package com.teclab.practicas.WikiBackend;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.auth.RegisterConverter;
import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.repository.UserRepository;
import com.teclab.practicas.WikiBackend.service.UserDetailServiceImpl;
import com.teclab.practicas.WikiBackend.service.UserServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RolesRepository rolesRepository;
    @Mock
    RegisterConverter registerConverter;
    @Mock
    UserDetailServiceImpl userDetailServiceImpl;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    AuthenticationManager authenticationManager;

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


    //REGISTER
    @Test
    void testCreateUser_shouldReturnSavedUser() {
        //GIVEN
        when(registerConverter.toEntity(any(RegisterRequestDto.class))).thenReturn(testUser);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("passwordEncriptada");
        when(rolesRepository.findByName(Roles.RoleName.ROLE_SUPER_USER)).thenReturn(Optional.of(getRole(Roles.RoleName.ROLE_SUPER_USER)));
        when(rolesRepository.findByName(Roles.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(getRole(Roles.RoleName.ROLE_ADMIN)));
        when(rolesRepository.findByName(Roles.RoleName.ROLE_COLLABORATOR)).thenReturn(Optional.of(getRole(Roles.RoleName.ROLE_COLLABORATOR)));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(registerConverter.toDto(any(User.class))).thenReturn(userRegisterResponseDto);

        // WHEN
        RegisterResponseDto createdUser;
        try {
            createdUser = userService.createUser(userRegisterRequestDto);
        } catch (Exception e) {
            createdUser = null;
        }

        // THEN
        assertNotNull(createdUser);
        assertEquals(userRegisterRequestDto.getUsername(), createdUser.getUsername());
        assertEquals(userRegisterRequestDto.getEmail(), createdUser.getEmail());
        verify(userRepository, times(1)).existsByEmail(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    void testCreateUser_shouldReturnUserExisting() {
        //GIVEN
        when(registerConverter.toEntity(any(RegisterRequestDto.class))).thenReturn(testUser);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // WHEN
        RegisterResponseDto createdUser;
        Exception exception;
        try {
            createdUser = userService.createUser(userRegisterRequestDto);
            exception = null;
        } catch (Exception e) {
            createdUser = null;
            exception = e;
        }

        // THEN
        assertNull(createdUser);
        assertEquals(exception.getMessage(), "El email test@example.com ya está registrado.");
    }

    //LOGIN
    @Test
    void testLoginUser_shouldReturnJwt() {
        // GIVEN
        LoginRequestDto requestDto = new LoginRequestDto("example@hotmail.com", "Example123*");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateAccessToken(any(Authentication.class))).thenReturn("jwt-token-generado-ficticio");

        // WHEN
        LoginResponseDto responseDto = userService.loginUser(requestDto);

        // THEN
        assertNotNull(responseDto);
        assertEquals("jwt-token-generado-ficticio", responseDto.getAccessToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateAccessToken(any(Authentication.class));
    }
    @Test
    void testLoginUser_shouldReturnIncorrectCredentials() {
        // GIVEN
        LoginRequestDto requestDto = new LoginRequestDto("example@hotmail.com", "Example123*");

        // Simula el escenario de credenciales inválidas
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // WHEN & THEN
        // Usamos assertThrows para verificar que el método lanza la excepción esperada.
        assertThrows(BadCredentialsException.class, () -> userService.loginUser(requestDto));

        // Adicionalmente, podemos verificar que el método para generar el token nunca fue llamado.
        verify(jwtUtils, never()).generateAccessToken(any());
    }

    //LOGIN
    @Test
    void testRefreshToken_shouldReturnNewJwt() {
        // GIVEN
        String oldJwt = "old-refresh-token";
        String email = "example@hotmail.com";
        String newJwt = "new-refresh-token";
        UserDetails userDetails = mock(UserDetails.class);

        // Configuración de los mocks:
        // 1. Cuando se llame a getUsername con el token viejo, devuelve el email.
        when(jwtUtils.getUsername(oldJwt)).thenReturn(email);

        // 2. Cuando se llame a loadUserByUsername con el email, devuelve un UserDetails simulado.
        when(userDetailServiceImpl.loadUserByUsername(email)).thenReturn(userDetails);

        // 3. Cuando se llame a generateRefreshToken con los UserDetails, devuelve el nuevo token.
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn(newJwt);

        // WHEN
        RefreshResponseDto response = userService.refreshToken(oldJwt);

        // THEN
        assertNotNull(response);
        assertEquals(newJwt, response.getRefreshToken());

        // Verificamos que los métodos del flujo de éxito fueron llamados
        verify(jwtUtils).getUsername(oldJwt);
        verify(userDetailServiceImpl).loadUserByUsername(email);
        verify(jwtUtils).generateRefreshToken(userDetails);
    }
    @Test
    void testRefreshToken_shouldReturnExceptionJwtExpired() {
        // GIVEN
        String expiredJwt = "expired-token";

        // Simula la excepción que se lanzaría al intentar leer un token expirado.
        when(jwtUtils.getUsername(expiredJwt))
                .thenThrow(new ExpiredJwtException(null, null, "JWT expirado"));

        // WHEN & THEN
        // Verificamos que se lance la excepción esperada (RuntimeException en este caso,
        // ya que tu método la envuelve).
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.refreshToken(expiredJwt));

        // Opcionalmente, podemos verificar el mensaje de la excepción o su causa raíz.
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof ExpiredJwtException);

        // Verificamos que el flujo se detuvo.
        // Ni el userDetailServiceImpl ni el método de generación del token fueron llamados.
        verify(userDetailServiceImpl, never()).loadUserByUsername(anyString());
        verify(jwtUtils, never()).generateRefreshToken(any());
    }

}