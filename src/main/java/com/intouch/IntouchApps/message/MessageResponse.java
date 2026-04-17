package com.intouch.IntouchApps.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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
    private Instant createdDateTime;
    private Instant lastModifiedDateTime;
}
