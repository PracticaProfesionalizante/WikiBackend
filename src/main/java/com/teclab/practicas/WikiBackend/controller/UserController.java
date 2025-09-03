package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;
import com.teclab.practicas.WikiBackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        UserResponseDto userResponseDto = (UserResponseDto) authentication.getPrincipal();
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {               //preguntar???
        return ResponseEntity.ok(userService.getAllUsers());          //hacer luego del comit de erich LISTA
    }


}
