package com.intouch.IntouchApps.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.mail.MessagingException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//import java.security.SignatureException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.intouch.IntouchApps.handler.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Component
public class GlobalExceptionHandler {

@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ExceptionResponse> handleException(RuntimeException exp){
//    System.out.println("RuntimeException thrown ===>");
    return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .body(
                    ExceptionResponse.builder()
                            .businessErrorCode(APPLICATION_ERROR.getCode())
                            .businessErrorDescription(APPLICATION_ERROR.getDescription())
                            .error(exp.getMessage())
                            .build()
            );
}
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp){
//        System.out.println("LockedException thrown ===>");
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(ACCOUNT_LOCKED.getDescription())
                                .build()
                );
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exp){
//        System.out.println("BadCredentialsException thrown ===>");
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(UsernameNotFoundException exp){
//        System.out.println("UsernameNotFoundException thrown ===>");
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_NOT_FOUND.getCode())
                                .businessErrorDescription(ACCOUNT_NOT_FOUND.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ExceptionResponse> handleException(SignatureException exp){
//        System.out.println("SignatureException thrown ===>");
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(JWT_SIGNATURE_EXCEPTION.getCode())
                                .businessErrorDescription(JWT_SIGNATURE_EXCEPTION.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccessDeniedException exp){
//        System.out.println("AccessDeniedException thrown ===>");
        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCESS_DENIED_EXCEPTION.getCode())
                                .businessErrorDescription(ACCESS_DENIED_EXCEPTION.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionResponse> handleException(ExpiredJwtException exp){
//        System.out.println("ExpiredJwtException thrown ===>");
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(JWT_EXPIRED_EXCEPTION.getCode())
                                .businessErrorDescription(JWT_EXPIRED_EXCEPTION.getDescription())
                                .error(JWT_EXPIRED_EXCEPTION.getDescription())
                                .build()
                );
    }
    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccountNotActivatedException exp){
//        System.out.println("AccountNotActivatedException thrown ===>");
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_NOT_ACTIVATED.getCode())
                                .businessErrorDescription(ACCOUNT_NOT_ACTIVATED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp){
//        System.out.println("MessagingException thrown ===>");
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp){
//        System.out.println("MethodArgumentNotValidException thrown ===>");
        Set<String> errors = new HashSet<>();
        exp.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    var errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
                });
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .validationErrors(errors)
                                .build()
                );
    }
    @ExceptionHandler(AWSFileUploadException.class)
    @ResponseBody
    public ResponseEntity<ExceptionResponse> handleException(AWSFileUploadException exp){
//        System.out.println("AWSFileUploadException thrown ===>");
        exp.printStackTrace();
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(AWS_FILE_UPLOAD_NOT_ALLOWED.getCode())
                                .businessErrorDescription(AWS_FILE_UPLOAD_NOT_ALLOWED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<ExceptionResponse> handleDuplicateKeyException(DataIntegrityViolationException exp) {
//        System.out.println("DataIntegrityViolationException thrown ===>");
        ConstraintViolationException cause = (ConstraintViolationException) exp.getCause();
        String failedField = cause.getConstraintName();
        try {
            String[] failedFieldArr= failedField.split("_");
            failedField = failedFieldArr[2];
        }catch (Exception ex){

        }

//        System.out.println(cause);
        return ResponseEntity
                .status(CONFLICT)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(APPLICATION_ERROR.getCode())
                                .businessErrorDescription("Duplicate entry found.")
                                .error("Your entry to " + failedField + " is taken")
//                                .error(cause.getErrorMessage())
                                .build()
                );
    }
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ExceptionResponse> handleException(Exception exp){
        // todo log the exception
//        System.out.println("Generic Exception thrown ===>");
        exp.printStackTrace();
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(APPLICATION_ERROR.getCode())
                                .businessErrorDescription("Internal error, contact the admin")
                                .error(exp.getMessage())
                                .build()
                );
    }
}
