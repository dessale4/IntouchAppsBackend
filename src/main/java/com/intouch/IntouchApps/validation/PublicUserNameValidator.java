package com.intouch.IntouchApps.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PublicUserNameValidator implements ConstraintValidator<ValidPublicUserName, String> {
    @Override
    public boolean isValid(String userName, ConstraintValidatorContext constraintValidatorContext) {
        return !userName.contains("@") && userName.length() >= 4 && userName.length() <= 10;
    }
}
