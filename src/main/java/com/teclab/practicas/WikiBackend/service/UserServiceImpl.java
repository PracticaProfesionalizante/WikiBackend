package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.config.JwtUtils;
import com.teclab.practicas.WikiBackend.converter.auth.RegisterConverter;
import com.teclab.practicas.WikiBackend.dto.auth.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            UserDetailServiceImpl userDetailServiceImpl,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.registerConverter = registerConverter;
        this.userDetailServiceImpl = userDetailServiceImpl;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
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
    public RefreshResponseDto refreshToken(String jwt){

        String email = jwtUtils.getUsername(jwt);

        UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(email);

        String newJwt = jwtUtils.generateRefreshToken(userDetails);

        RefreshResponseDto response = new RefreshResponseDto();
        response.setRefreshToken(newJwt);

        return response;
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
    private Set<Roles> getRoles (Set<String> roles){
        return roles.stream()
                .map(roleName -> {
                    Roles.RoleName enumRole = Roles.RoleName.valueOf("ROLE_" + roleName.toUpperCase());
                    return rolesRepository.findByName(enumRole)
                            .orElseThrow(() -> new IllegalArgumentException("Rol inválido en el DTO: " + roleName));
                })
                .collect(Collectors.toSet());
    }
}