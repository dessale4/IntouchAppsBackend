package com.intouch.IntouchApps.message;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageReplyDTO {
    private Integer id;
    private String sendingEmail;
    private String sendingUsername;
    private String message;
    private LocalDateTime createdDate;
}
