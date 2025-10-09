package com.teclab.practicas.WikiBackend.service.user;

import com.teclab.practicas.WikiBackend.dto.auth.*;
import com.teclab.practicas.WikiBackend.dto.user.*;

import java.util.List;

public interface UserService {
    // Authentication related methods
    public RegisterResponseDto createUser(RegisterRequestDto newUserDto);
    public LoginResponseDto loginUser(LoginRequestDto request);
    public RefreshResponseDto refreshToken(String jwt);
    //public void logout(String refreshToken);

    // User management methods
    public List<UserResponseDto> getAllUsers();
    public UserResponseDto getMyUser(String token);
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto updateUserDto);
    public void deleteUser(Long userId);
}