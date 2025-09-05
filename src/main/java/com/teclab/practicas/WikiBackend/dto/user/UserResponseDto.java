package com.teclab.practicas.WikiBackend.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Nombre de usuario", example = "juan.perez")
    private String username;

    @Schema(description = "Email del usuario", example = "email123@gmail.com")
    private String email;

    private Boolean enabled;

    @Schema(description = "Roles asignados al usuario")
    private Set<String> roles;

    @Schema(description = "Fecha de creación del usuario")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de la última actualización del usuario")
    private LocalDateTime updatedAt;

}






