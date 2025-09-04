package com.teclab.practicas.WikiBackend.service;

import com.teclab.practicas.WikiBackend.dto.auth.*;

public interface UserService {
    public RegisterResponseDto createUser(RegisterRequestDto newUserDto);
    public LoginResponseDto loginUser(LoginRequestDto request);
    public RefreshResponseDto refreshToken(String jwt);
}