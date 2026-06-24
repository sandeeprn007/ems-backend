package net.javaguides.ems.service;

import net.javaguides.ems.dto.JwtAuthResponse;
import net.javaguides.ems.dto.LoginDto;
import net.javaguides.ems.dto.RegisterDto;
import net.javaguides.ems.dto.ChangePasswordDto;

public interface AuthService {

    JwtAuthResponse login(LoginDto loginDto);

    String register(RegisterDto registerDto);

    String registerAdmin(RegisterDto registerDto);

    String changePassword(ChangePasswordDto changePasswordDto);
}
