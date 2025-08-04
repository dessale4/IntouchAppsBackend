package com.intouch.IntouchApps.message;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
    @NotEmpty(message = "Email is a required field")
    @NotBlank(message = "Email is a required field")
    @Email(message = "Invalid email format")
    private String sendingEmail;
    private String receivingEmail;
    @NotEmpty(message = "Username is a required field")
    @NotBlank(message = "Username is a required field")
    private String sendingUsername;
    private String receivingUsername;
//    @Lob
//    @Size(max = 500)
    @NotEmpty(message = "Message is a required field")
    @NotBlank(message = "Message is a required field")
    private String message;
}
