package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.validation.ValidMinimumAge;
import com.intouch.IntouchApps.validation.ValidPublicUserName;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Builder
public class RegistrationRequest {
//    @NotEmpty(message = "Firstname is a required field")
    @NotBlank(message = "Firstname is a required field")
    private String firstName;
//    @NotEmpty(message = "Lastname is a required field")
    @NotBlank(message = "Lastname is a required field")
    private String lastName;
    @Pattern(
            regexp = "^\\+?[1-9]\\d{6,14}$",
            message = "Phone number must be a valid international number."
    )
    private String phoneNumber;
    @NotBlank(message = "UserName is a required field")
    @Pattern(regexp = "[A-Za-z]+", message = "Username should include only alphabetic string")
    @ValidPublicUserName
    private String userName;
//    @NotEmpty(message = "Email is a required field")
    @NotBlank(message = "Email is a required field")
    @Email(message = "Invalid email format")
    private String email;
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @ValidMinimumAge(value = 13, message = "You must be at least 13 years old")
    private LocalDate dateOfBirth;
//    @NotEmpty(message = "Password is a required field")
    @NotBlank(message = "Password is a required field")
    @Size(min = 6, max = 10, message = "Password length should be between 6 and 10 characters")
    private String password;
}
