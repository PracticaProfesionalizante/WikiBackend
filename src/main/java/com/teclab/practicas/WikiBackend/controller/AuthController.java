package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(
        summary = "Iniciar Sesion",
        description = "Genera y Devuelve un JWT para validar futuras acciones."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inicio de Sesion con exito",
                content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
        @ApiResponse(responseCode = "422", description = "Argumento no valido",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> createAuthenticationToken(@Valid @RequestBody LoginRequestDto request) throws Exception {

        LoginResponseDto token = userService.loginUser(request);

        return ResponseEntity.ok(token);
    }

    @Operation(
            summary = "Crear Cuenta",
            description = "Genera un usuario nuevo en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado con exito",
                    content = @Content(schema = @Schema(implementation = RegisterRequestDto.class))),
            @ApiResponse(responseCode = "422", description = "Argumento no valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la peticion invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto newUserDto) {
        System.out.println("registerUser: "
                + newUserDto.getUsername() + " / "
                + newUserDto.getEmail() + " / "
                + newUserDto.getPassword() + " / "
                + newUserDto.getRoles().toString()
        );
        RegisterResponseDto response = userService.createUser(newUserDto);
        System.out.println("response: " + response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/refresh")
    public ResponseEntity<RefreshResponseDto> refreshToken(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7);

        RefreshResponseDto response = userService.refreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }
}