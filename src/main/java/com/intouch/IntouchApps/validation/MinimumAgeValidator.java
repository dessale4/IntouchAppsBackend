package com.intouch.IntouchApps.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;

public class MinimumAgeValidator implements ConstraintValidator<ValidMinimumAge, LocalDate> {
    private int minAge;
    private final Clock clock;

    public MinimumAgeValidator() {
        this.clock = Clock.systemUTC();
    }

    // Optional constructor if later you want injection/testing
    public MinimumAgeValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void initialize(ValidMinimumAge constraintAnnotation) {
        this.minAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return true; // let @NotNull handle null
        }
        LocalDate today = LocalDate.now(clock);

        // valid if dob is on or before today's date minus minAge years
        return !dob.isAfter(today.minusYears(minAge));
    }
}
