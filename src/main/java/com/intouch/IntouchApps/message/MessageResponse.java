package com.intouch.IntouchApps.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageResponse {
    private Integer messageId;
    private String sendingEmail;
    private String receivingEmail;
    private String sendingUsername;
    private String receivingUsername;
    private String message;
    private List<MessageReplyDTO> messageReplies = new ArrayList<>();
    private LocalDate createdDate;
    private LocalTime createdTime;
    private LocalDateTime createdDateTime;
    private LocalDate lastModifiedDate;
    private LocalTime lastModifiedTime;
    private LocalTime lastModifiedDateTime;
}
