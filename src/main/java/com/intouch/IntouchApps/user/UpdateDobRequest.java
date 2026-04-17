package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.validation.ValidMinimumAge;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UpdateDobRequest(
        @NotNull
        @Past
        @ValidMinimumAge(13)
        LocalDate dateOfBirth
) {}
