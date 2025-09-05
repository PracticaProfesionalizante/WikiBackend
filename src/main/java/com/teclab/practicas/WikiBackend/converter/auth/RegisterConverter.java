package com.teclab.practicas.WikiBackend.converter.auth;

import com.teclab.practicas.WikiBackend.dto.auth.RegisterRequestDto;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;
import com.teclab.practicas.WikiBackend.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RegisterConverter {

    public User toEntity(RegisterRequestDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setEnabled(true);
        
        return user;
    }

    public RegisterResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }

        RegisterResponseDto dto = new RegisterResponseDto();
        dto.setId(user.getId() != null ? user.getId() : null);
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Mapeamos el Set<Roles> a un Set<String> con los nombres de los roles
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
}