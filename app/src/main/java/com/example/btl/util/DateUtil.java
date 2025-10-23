package com.example.btl.util;

import java.util.Calendar;

public class DateUtil {

    /**
     * Trả về thời gian bắt đầu của một ngày (00:00:00)
     * @param millis Thời gian (ms) bất kỳ trong ngày đó
     * @return Thời gian (ms) lúc 00:00:00 của ngày
     */
    public static long getStartOfDayMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}