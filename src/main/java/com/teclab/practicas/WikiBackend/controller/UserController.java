package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;
import com.teclab.practicas.WikiBackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Autenticación", description = "Poder loguearse, registrarse y actualizar token")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Iniciar Sesión",
            description = "Genera y devuelve un token de acceso con una vigencia de 30 min."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de Sesion con exito",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la peticion invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado: Token JWT no proporcionado o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Argumento no valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        UserResponseDto userResponseDto = userService.getMyUser(token);
        return ResponseEntity.ok(userResponseDto);
    }
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Devuelve una lista de todos los usuarios registrados en el sistema. Advierte si no tenes el rol necesario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de Sesion con exito",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la peticion invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado: Token JWT no proporcionado o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido: El usuario no tiene el rol necesario",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Argumento no valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })

    @GetMapping
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //@SecurityRequirement(name = "Bearer Authentication")


}
