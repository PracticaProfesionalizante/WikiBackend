package com.teclab.practicas.WikiBackend.service.user;

import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {
    public RegisterResponseDto createUser(RegisterRequestDto newUserDto);
    public LoginResponseDto loginUser(LoginRequestDto request);
    public RefreshResponseDto refreshToken(String jwt);
    //public void logout(String refreshToken);

    public List<UserResponseDto> getAllUsers();
    public UserResponseDto getMyUser(String token);
}