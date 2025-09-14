package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemRequestDto;
import com.teclab.practicas.WikiBackend.dto.menu.MenuItemResponseDto;
import com.teclab.practicas.WikiBackend.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/menu")
@Validated
public class MenuController {

    @Autowired
    private MenuService menuService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MenuItemResponseDto> createMenuItem(
            @Valid @RequestBody MenuItemRequestDto requestDto) {
        System.out.println("requestDto - " + requestDto);
        MenuItemResponseDto responseDto = menuService.createMenuItem(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
            summary = "Agregamos nuevo menu",
            description = "Generamo un elemento nuevo en el menu"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Elemento agregado al menu con exito",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la peticion invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))  //401, 500,403, 422
    })


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MenuItemResponseDto> editMenuItem(
            @PathVariable Long id, @Valid @RequestBody MenuItemRequestDto requestDto) {
        MenuItemResponseDto responseDto = menuService.updateMenuItem(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Actualiza un ítem del menú",
            description = "Permite cambiar el nombre y/o orden un elemento del menú. Solo para SuperUser." //200,401, 500,403, 422
    )

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Elimina un ítem del menú",
            description = "Elimina un elemento del menú por su ID. Solo para SuperUser." //200,401, 500,403, 422
    )


    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<MenuItemResponseDto>> getMenu(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);

        List<MenuItemResponseDto> menu = menuService.getMenuByRoles(token);

        return ResponseEntity.ok(menu);
    }


}
