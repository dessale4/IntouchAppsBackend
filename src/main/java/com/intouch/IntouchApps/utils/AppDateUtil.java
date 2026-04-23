package com.intouch.IntouchApps.utils;

import java.time.Instant;

public class AppDateUtil {
    public static Instant getCurrentUtcInstant() {
        return Instant.now();
    }
    private AppDateUtil() {
    }
}
