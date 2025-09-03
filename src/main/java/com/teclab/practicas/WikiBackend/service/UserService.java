package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.auth.RegisterRequestDto;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;
import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {
    public RegisterResponseDto createUser(RegisterRequestDto newUserDto);

    public List<UserResponseDto> getAllUsers();

    public UserResponseDto getMyUser();


}