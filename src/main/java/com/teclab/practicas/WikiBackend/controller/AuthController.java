package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.config.AuthEntryPointJwt;
import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
@Tag(name = "Autenticacion", description = "Poder loguearse, registrarse y actualizar token")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Iniciar Sesion",
            description = "Genera y devuelve un token de acceso con una vigencia de 30 min."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de Sesion con exito",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Argumento no valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la peticion invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error del cliente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) throws Exception {
        LoginResponseDto token = userService.loginUser(request);
        return ResponseEntity.ok(token);
    }

    @Operation(
            summary = "Crear Cuenta",
            description = "Genera un usuario nuevo en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado con exito",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la peticion invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Usuario sin loguearse",
                    content = @Content(schema = @Schema(implementation = AuthEntryPointJwt.class))),
            @ApiResponse(responseCode = "409", description = "Usuario Existente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Argumento no valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error del cliente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_SUPER_USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<RegisterResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto newUserDto) {
        RegisterResponseDto response = userService.createUser(newUserDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(
            summary = "Refresh Token",
            description = "Obtener Token de larga duracion ( 7 dias )"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token actualizado con exito",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error del cliente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/refresh")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<RefreshResponseDto> refreshToken(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7);
        RefreshResponseDto response = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}