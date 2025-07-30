package org.example.util;

import java.time.ZoneId;

public class TimeZoneContext {
    private static final ThreadLocal<ZoneId> currentTimeZone = new ThreadLocal<>();

    public static void setCurrentTimeZone(ZoneId timeZone) {
        currentTimeZone.set(timeZone);
    }

    public static ZoneId getCurrentTimeZone() {
        ZoneId timeZone = currentTimeZone.get();
        return timeZone != null ? timeZone : ZoneId.of("UTC");
    }

    public static void clear() {
        currentTimeZone.remove();
    }
}
