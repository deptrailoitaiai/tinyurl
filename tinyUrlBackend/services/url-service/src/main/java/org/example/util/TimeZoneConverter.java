package org.example.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeZoneConverter {

    /**
     * Convert LocalDateTime từ UTC sang timezone của user
     */
    public static LocalDateTime convertToUserTimeZone(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return null;

        ZoneId userTimeZone = TimeZoneContext.getCurrentTimeZone();

        // Convert UTC LocalDateTime sang ZonedDateTime rồi chuyển sang user timezone
        return utcDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(userTimeZone)
                .toLocalDateTime();
    }

    /**
     * Convert từ user timezone về UTC để lưu database
     */
    public static LocalDateTime convertToUTC(LocalDateTime userDateTime) {
        if (userDateTime == null) return null;

        ZoneId userTimeZone = TimeZoneContext.getCurrentTimeZone();

        return userDateTime.atZone(userTimeZone)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * Format datetime theo timezone của user
     */
    public static String formatForUser(LocalDateTime utcDateTime, String pattern) {
        LocalDateTime userDateTime = convertToUserTimeZone(utcDateTime);
        return userDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}