package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.timezone.TimeZoneContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Set;

@Component
public class TimeZoneFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TimeZoneFilter.class);
    private static final String TIMEZONE_HEADER = "X-User-Timezone";
    private static final String DEFAULT_TIMEZONE = "UTC";

    // Cache các timezone hợp lệ để tránh exception
    private static final Set<String> VALID_TIMEZONES = ZoneId.getAvailableZoneIds();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String timeZoneId = extractTimeZone(request);
            ZoneId zoneId = parseTimeZone(timeZoneId);
            TimeZoneContext.setCurrentTimeZone(zoneId);

            logger.debug("Set timezone for request: {}", zoneId.getId());

            filterChain.doFilter(request, response);
        } finally {
            TimeZoneContext.clear();
        }
    }

    private String extractTimeZone(HttpServletRequest request) {
        // Thứ tự ưu tiên: Header -> Query Parameter -> Default
        String timeZone = request.getHeader(TIMEZONE_HEADER);

        if (isEmpty(timeZone)) {
            timeZone = request.getParameter("timezone");
        }

        return isEmpty(timeZone) ? DEFAULT_TIMEZONE : timeZone.trim();
    }

    private ZoneId parseTimeZone(String timeZoneId) {
        if (isEmpty(timeZoneId) || !VALID_TIMEZONES.contains(timeZoneId)) {
            logger.warn("Invalid timezone: '{}', using UTC instead", timeZoneId);
            return ZoneId.of(DEFAULT_TIMEZONE);
        }

        try {
            return ZoneId.of(timeZoneId);
        } catch (Exception e) {
            logger.warn("Error parsing timezone: '{}', using UTC instead. Error: {}",
                    timeZoneId, e.getMessage());
            return ZoneId.of(DEFAULT_TIMEZONE);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}