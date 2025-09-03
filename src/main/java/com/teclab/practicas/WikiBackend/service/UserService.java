package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.auth.RegisterRequestDto;
import com.teclab.practicas.WikiBackend.dto.auth.RegisterResponseDto;

public interface UserService {
    public RegisterResponseDto createUser(RegisterRequestDto newUserDto);

}

//    public List<UserDto> getUserAll();
//    public UserDto getUser();