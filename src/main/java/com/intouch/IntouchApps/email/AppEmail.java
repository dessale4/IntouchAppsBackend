package com.intouch.IntouchApps.email;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
