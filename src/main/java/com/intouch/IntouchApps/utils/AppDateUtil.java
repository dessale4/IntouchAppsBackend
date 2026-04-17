package com.intouch.IntouchApps.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AppDateUtil {
    public static Instant getCurrentUTCLocalDateTime() {
        return Instant.now();
    }
    public static Instant localDateTimeToInstant(LocalDateTime localDateTime, String timeZone) {
        return localDateTime.atZone(ZoneId.of(timeZone))
                .toInstant();
    }
    private AppDateUtil() {
    }
}
