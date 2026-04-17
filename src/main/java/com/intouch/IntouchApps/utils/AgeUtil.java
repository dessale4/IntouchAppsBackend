package com.intouch.IntouchApps.utils;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

public final class AgeUtil {

    private AgeUtil() {
    }
    public static int calculateAge(LocalDate dateOfBirth) {
        return calculateAge(dateOfBirth, Clock.systemUTC());
    }

    public static int calculateAge(LocalDate dateOfBirth, Clock clock) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("dateOfBirth cannot be null");
        }

        LocalDate today = LocalDate.now(clock);

        if (dateOfBirth.isAfter(today)) {
            throw new IllegalArgumentException("dateOfBirth cannot be in the future");
        }
        return Period.between(dateOfBirth, today).getYears();
    }
}
