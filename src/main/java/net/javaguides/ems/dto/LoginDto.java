package net.javaguides.ems.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

    @NotBlank(message = "Email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
