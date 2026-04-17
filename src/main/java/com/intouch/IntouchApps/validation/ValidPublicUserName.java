package com.intouch.IntouchApps.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PublicUserNameValidator.class)
public @interface ValidPublicUserName {
    public String message() default "Username length should be 4 to 10 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
