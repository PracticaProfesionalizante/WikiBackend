package com.teclab.practicas.WikiBackend.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;
import com.teclab.practicas.WikiBackend.entity.User;
import com.teclab.practicas.WikiBackend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}


