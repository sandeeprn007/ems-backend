package net.javaguides.ems.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.ems.dto.ChangePasswordDto;
import net.javaguides.ems.dto.JwtAuthResponse;
import net.javaguides.ems.dto.LoginDto;
import net.javaguides.ems.dto.RegisterDto;
import net.javaguides.ems.entity.Role;
import net.javaguides.ems.entity.User;
import net.javaguides.ems.repository.RoleRepository;
import net.javaguides.ems.repository.UserRepository;
import net.javaguides.ems.security.JwtTokenProvider;
import net.javaguides.ems.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new JwtAuthResponse(token, "Bearer", authentication.getName(), roles);
    }

    @Override
    public String register(RegisterDto registerDto) {
        validateNormalRegistration(registerDto);
        registerUserWithRole(registerDto, "ROLE_USER");
        return "User registered successfully!";
    }

    @Override
    public String registerAdmin(RegisterDto registerDto) {
        registerUserWithRole(registerDto, "ROLE_ADMIN");
        return "Admin registered successfully!";
    }

    @Override
    public String changePassword(ChangePasswordDto changePasswordDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new IllegalArgumentException("Logged in user was not found"));

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (changePasswordDto.getNewPassword() == null || changePasswordDto.getNewPassword().trim().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully!";
    }

    private void registerUserWithRole(RegisterDto registerDto, String roleName) {
        String username = resolveUsername(registerDto);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Default role is not configured: " + roleName));

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(username);
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setAuthProvider("LOCAL");
        user.setRoles(Set.of(role));

        userRepository.save(user);
    }

    private void validateNormalRegistration(RegisterDto registerDto) {
        if (registerDto.getConfirmPassword() == null || registerDto.getConfirmPassword().isBlank()) {
            throw new IllegalArgumentException("Confirm password is required");
        }

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        if (!registerDto.getEmail().toLowerCase().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Please enter a valid Gmail address.");
        }
    }

    private String resolveUsername(RegisterDto registerDto) {
        if (registerDto.getUsername() != null && !registerDto.getUsername().isBlank()) {
            return registerDto.getUsername().trim();
        }

        return registerDto.getEmail().substring(0, registerDto.getEmail().indexOf('@')).trim();
    }
}
