package com.teclab.practicas.WikiBackend.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    @Size(min = 4, max = 20, message = "El nombre de usuario debe tener entre 4 y 20 caracteres.")
//    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "El nombre de usuario solo puede contener letras y números.")
    private String username;

    @NotBlank(message = "El correo electrónico no puede estar vacío.")
    @Email(message = "El correo electrónico tiene un formato incorrecto.")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial.")
    private String password;

    @NotEmpty(message = "La lista de roles no puede estar vacía.")
    private Set<String> roles;
}