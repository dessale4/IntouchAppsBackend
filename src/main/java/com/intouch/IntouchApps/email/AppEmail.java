package com.intouch.IntouchApps.email;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppEmail {
    private String to;
    private String username;
    private EmailTemplateName emailTemplate;
    private String confirmationUrl;
    private String activationCode;
    private String subject;
    private String messageTitle;
    private String confirmationText;
    private String message;
}
