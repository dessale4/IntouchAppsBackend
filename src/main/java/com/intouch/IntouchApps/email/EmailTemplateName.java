package com.intouch.IntouchApps.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    GENERIC_EMAIL_TEMPLATE("generic_email_template"),
    CONFIRM_EMAIL("confirm_email");
    private final String name;
    EmailTemplateName(String name){
        this.name = name;
    }
}
