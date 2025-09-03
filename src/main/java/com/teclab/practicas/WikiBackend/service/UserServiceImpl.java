package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.converter.auth.RegisterConverter;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterRequestDto;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.exception.EmailIsExistente;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final RegisterConverter registerConverter;
    private final PasswordEncoder passwordEncoder;

    // Inyección de dependencias
    public UserServiceImpl(
            UserRepository userRepository,
            RolesRepository rolesRepository,
            RegisterConverter registerConverter,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.registerConverter = registerConverter;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public RegisterResponseDto createUser(RegisterRequestDto userDto) {
        userExists(userDto.getEmail());

        User newUser = registerConverter.toEntity(userDto);
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setRoles(getRoles(userDto.getRoles()));

        User userCreated = userRepository.save(newUser);

        return registerConverter.toDto(userCreated);
    }

    public void userExists(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new EmailIsExistente("El email " + email + " ya está registrado.");
        }
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