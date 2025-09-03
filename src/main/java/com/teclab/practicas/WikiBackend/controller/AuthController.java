package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.auth.RegisterRequestDto;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;


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

        RegisterResponseDto response = userService.createUser(newUserDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}


//
//    @Operation(
//            summary = "Iniciar Sesion",
//            description = "Genera y Devuelve un JWT para validar futuras acciones."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Inicio de Sesion con exito",
//                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
//            @ApiResponse(responseCode = "422", description = "Argumento no valido",
//                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
//    })
//    @PostMapping("/login")
//    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequestDTO authRequestDTO) throws Exception {
//        System.out.println("Login attempt: " + authRequestDTO.getEmail() + " / " + authRequestDTO.getPassword());
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(authRequestDTO.getEmail(), authRequestDTO.getPassword())
//            );
//        } catch (BadCredentialsException e) {
//            throw new Exception("Credenciales incorrectas", e);
//        }
//
//        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequestDTO.getEmail());
//        final String jwt = jwtUtil.generateToken(userDetails);
//
//        return ResponseEntity.ok(new AuthResponseDTO(jwt));
//    }
//
//    @Operation(
//            summary = "Iniciar Sesion",
//            description = "Genera y Devuelve un JWT para validar futuras acciones."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Inicio de Sesion con exito",
//                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
//            @ApiResponse(responseCode = "422", description = "Argumento no valido",
//                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
//    })
//    @PostMapping("/refresh")
//    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequestDTO authRequestDTO) throws Exception {
//        System.out.println("Login attempt: " + authRequestDTO.getEmail() + " / " + authRequestDTO.getPassword());
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(authRequestDTO.getEmail(), authRequestDTO.getPassword())
//            );
//        } catch (BadCredentialsException e) {
//            throw new Exception("Credenciales incorrectas", e);
//        }
//
//        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequestDTO.getEmail());
//        final String jwt = jwtUtil.generateToken(userDetails);
//
//        return ResponseEntity.ok(new AuthResponseDTO(jwt));
//    }