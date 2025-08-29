package com.intouch.IntouchApps.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum BusinessErrorCodes {
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"),
    AWS_FILE_UPLOAD_NOT_ALLOWED(306, FORBIDDEN, "AWS file upload error"),
    ACCOUNT_NOT_ACTIVATED(305, FORBIDDEN, "Activate your account before login"),
    BAD_CREDENTIALS(401, FORBIDDEN, "Email and / or Password is incorrect"),
    JWT_SIGNATURE_EXCEPTION(403, FORBIDDEN, "JWT Signature not valid"),
    JWT_EXPIRED_EXCEPTION(403, FORBIDDEN, "Login session already expired. Please login again."),
    ACCESS_DENIED_EXCEPTION(403, FORBIDDEN, "not_authorized!"),
    APPLICATION_ERROR(500, INTERNAL_SERVER_ERROR, "Some thing went wrong"),
    ;
    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;

    BusinessErrorCodes(int code, HttpStatus status, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = status;
    }
}
