package com.teclab.practicas.WikiBackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
    private String username;
    private String email;
    private Boolean enabled;
    private String password;
    private Set<String> roles;
}
