package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.auth.RegisterConverter;
import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.exception.EmailIsExistente;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.repository.UserRepository;
import jakarta.transaction.Transactional;
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
        System.out.println("createUser before userExist: "
                + userDto.getUsername() + " / "
                + userDto.getEmail() + " / "
                + userDto.getPassword() + " / "
                + userDto.getRoles().toString()
        );
        userExists(userDto.getEmail());
        System.out.println("createUser after userExist: ");

        User newUser = registerConverter.toEntity(userDto);
        System.out.println("createUser after toEntity: ");

        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        System.out.println("createUser after encode: ");

        newUser.setRoles(getRoles(userDto.getRoles()));
        System.out.println("createUser after getRoles: ");

        User userCreated = userRepository.save(newUser);
        System.out.println("createUser after save: ");

        return registerConverter.toDto(userCreated);
    }

    private void userExists(String email) {
        System.out.println("userExists before repo: " + email);
        boolean existingUser = userRepository.existsByEmail(email);
        System.out.println("userExists after repo: " + existingUser);

        if (existingUser) {
            System.out.println("existingUser.isPresent()");
            throw new EmailIsExistente("El email " + email + " ya está registrado.");
        }
        System.out.println("userExists after if: " + existingUser);
    }

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
        System.out.println("Login attempt: " + request.getEmail() + " / " + request.getPassword());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtUtils.generateAccessToken(userDetails);

        LoginResponseDto response = new LoginResponseDto();
        response.setAccessToken(jwt);

        return response;
    }

    @Override
    public RefreshResponseDto refreshToken(String jwt) {

        String email = jwtUtils.getUsername(jwt);

        UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(email);

        String newJwt = jwtUtils.generateRefreshToken(userDetails);

        RefreshResponseDto response = new RefreshResponseDto();
        response.setRefreshToken(newJwt);

        return response;
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