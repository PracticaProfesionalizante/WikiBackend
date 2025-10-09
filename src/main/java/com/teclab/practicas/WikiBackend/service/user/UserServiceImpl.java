package com.teclab.practicas.WikiBackend.service.user;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.auth.RegisterConverter;
import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.dto.user.UpdateUserRequestDto;
import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.exception.EmailIsExistente;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final RegisterConverter registerConverter;

    private final UserDetailServiceImpl userDetailServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    // Inyección de dependencias
    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            RolesRepository rolesRepository,
            RegisterConverter registerConverter,
            JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailServiceImpl userDetailServiceImpl) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.registerConverter = registerConverter;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailServiceImpl = userDetailServiceImpl;
    }

    @Override
    @Transactional
    public RegisterResponseDto createUser(RegisterRequestDto userDto) {
        try {
            User newUser = registerConverter.toEntity(userDto);

            if (newUser == null) throw new IllegalArgumentException("Body Corrupto");

            userExists(newUser);

            newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

            newUser.setRoles(getRoles(userDto.getRoles()));

            User userCreated = userRepository.save(newUser);

            return registerConverter.toDto(userCreated);
        } catch (Exception e) {
            System.out.println("createUser" + e);
            throw e;
        }
    }


    @Override
    public void deleteUser(Long userId) {
        // Verificar si el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userId));

        // Eliminar al usuario
        userRepository.delete(user);

        // Registrar la acción
        log.info("Usuario eliminado con éxito. ID: {}", userId);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto updateUserDto) {
        // Buscar el usuario existente
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userId));

        // Actualizar username si se proporciona
        if (updateUserDto.getUsername() != null && !updateUserDto.getUsername().isBlank()) {
            user.setUsername(updateUserDto.getUsername());
        }

        // Actualizar email si se proporciona
        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isBlank()) {
            // Verificar si el nuevo email ya está en uso
            if (userRepository.existsByEmail(updateUserDto.getEmail()) &&
                    !user.getEmail().equals(updateUserDto.getEmail())) {
                throw new EmailIsExistente("El correo electrónico ya está en uso");
            }
            user.setEmail(updateUserDto.getEmail());
        }

        // Actualizar enabled si se proporciona
        if (updateUserDto.getEnabled() != null) {
            user.setEnabled(updateUserDto.getEnabled());
        }

        // Actualizar contraseña si se proporciona
        if (updateUserDto.getPassword() != null && !updateUserDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        }

        // Actualizar roles si se proporcionan
        if (updateUserDto.getRoles() != null && !updateUserDto.getRoles().isEmpty()) {
            Set<Roles> roles = updateUserDto.getRoles().stream()
                    .map(role -> rolesRepository.findByName(Roles.RoleName.valueOf(role))
                            .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + role)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        // Guardar los cambios
        User updatedUser = userRepository.save(user);

        // Convertir a DTO y devolver
        return toUserResponseDto(updatedUser);
    }

//    @Override
//    public void logout(String refreshToken) {
//        // JWT stateless: no se guarda estado en servidor. Validamos y registramos el intento.
//        try {
//            jwtUtils.validateJwtToken(refreshToken);
//            String email = jwtUtils.getUsername(refreshToken);
//            log.info("Logout solicitado con refresh token válido para usuario: {}", email);
//        } catch (JwtException e) {
//            // Token inválido/expirado: igualmente respondemos 200 por idempotencia
//            log.warn("Logout con refresh token inválido/expirado: {}", e.getMessage());
//        } catch (Exception e) {
//            log.error("Error inesperado durante logout", e);
//        }
//        // Para invalidación real, implementar blacklist en almacenamiento externo (Redis/DB)
//    }

    @Transactional
    private void userExists(User user) {
        boolean userByEmail = userRepository.existsByEmail(user.getEmail());

        if (userByEmail) {
            throw new EmailIsExistente("El email " + user.getEmail() + " ya está registrado.");
        }
    }

    @Transactional
    private Set<Roles> getRoles(Set<String> roles) {
        return roles.stream()
                .map(roleName -> {
                    Roles.RoleName enumRole = Roles.RoleName.valueOf("ROLE_" + roleName.toUpperCase());
                    return rolesRepository.findByName(enumRole)
                            .orElseThrow(() -> new IllegalArgumentException("Rol inválido en el DTO: " + roleName));
                })
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto request) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
            );

            String jwt = jwtUtils.generateAccessToken(authentication);

            LoginResponseDto response = new LoginResponseDto();
            response.setAccessToken(jwt);

            return response;

        } catch (Exception e) {
            System.out.println("loginUser " + e);
            throw e;
        }
    }

    @Override
    public RefreshResponseDto refreshToken(String jwt) {
        try {
            String email = jwtUtils.getUsername(jwt);

            UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(email);

            String newJwt = jwtUtils.generateRefreshToken(userDetails);

            RefreshResponseDto response = new RefreshResponseDto();
            response.setRefreshToken(newJwt);

            return response;
        } catch (Exception e) {
            System.out.println("refreshToken" + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getMyUser(String token) {
        String email = jwtUtils.getUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró ningún usuario con ese email"));

        return toUserResponseDto(user);
    }

    private UserResponseDto toUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return dto;
    }
}