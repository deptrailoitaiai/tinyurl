package org.example.timezone;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UserTimeZoneLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateTimeString = p.getText();

        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        try {
                // Frontend gửi ISO 8601 format (có thể có timezone info hoặc UTC)
                // Ví dụ: "2025-07-31T19:00:00Z" hoặc "2025-07-31T19:00:00+07:00" hoặc "2025-07-31T19:00:00"

            if (dateTimeString.contains("T")) {
                // Là ISO 8601 format
                if (dateTimeString.endsWith("Z") || dateTimeString.matches(".*[+-]\\d{2}:\\d{2}$")) {
                    // Có timezone info - parse ZonedDateTime rồi chuyển về LocalDateTime
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString);
                    return zonedDateTime.toLocalDateTime();
                } else {
                    // Không có timezone info - parse trực tiếp LocalDateTime
                    return LocalDateTime.parse(dateTimeString);
                }
            } else {
                // Format khác (fallback) - "yyyy-MM-dd HH:mm:ss"
                return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

        } catch (Exception e) {
            // Nếu parse lỗi, thử các format khác
            try {
                return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                throw new IOException("Không thể parse datetime: " + dateTimeString, ex);
            }
        }
    }
}
