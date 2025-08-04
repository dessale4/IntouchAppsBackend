package com.intouch.IntouchApps.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RegistrationRequest {
    @NotEmpty(message = "Firstname is a required field")
    @NotBlank(message = "Firstname is a required field")
    private String firstName;
    @NotEmpty(message = "Lastname is a required field")
    @NotBlank(message = "Lastname is a required field")
    private String lastName;
    @NotEmpty(message = "UserName is a required field")
    @NotBlank(message = "UserName is a required field")
    private String userName;
    @NotEmpty(message = "Email is a required field")
    @NotBlank(message = "Email is a required field")
    @Email(message = "Invalid email format")
    private String email;
    @NotEmpty(message = "Password is a required field")
    @NotBlank(message = "Password is a required field")
    @Size(min=6, max=10, message = "Password length should be between 6 and 10 characters")
    private String password;
}
