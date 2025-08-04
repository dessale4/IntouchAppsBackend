package com.intouch.IntouchApps.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AWSFileUploadException  extends Exception{
    public AWSFileUploadException(String message){
        super(message);
    }
}
