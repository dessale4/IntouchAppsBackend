package com.intouch.IntouchApps.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class AppDateUtil {
    public static LocalDateTime getCurrentUTCLocalDateTime() {

        return LocalDateTime.now(ZoneId.of("UTC"));
    }


//    public static Date getDateFromCurrentUTCDateTime() throws ParseException {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
//        Date utcDate = localDateFormat.parse(simpleDateFormat.format(new Date()));//currentTimeMillis()
////        return utcDate.getTime();
//        return utcDate;
//    }
//    public static long getCurrentUTCTimeMillis() throws ParseException {
//        return getDateFromCurrentUTCDateTime().getTime();
//    }
    }
