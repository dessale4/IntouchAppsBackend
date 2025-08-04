package com.intouch.IntouchApps.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @NotEmpty(message = "Email is a required field")
    @NotBlank(message = "Email is a required field")
    @Email(message = "Invalid email format")
    private String email;
    @NotEmpty(message = "Password is a required field")
    @NotBlank(message = "Password is a required field")
    private String password;
}
