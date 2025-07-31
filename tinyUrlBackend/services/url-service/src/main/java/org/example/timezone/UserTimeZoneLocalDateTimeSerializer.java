package org.example.timezone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserTimeZoneLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    // Sử dụng ISO 8601 format cho output
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (localDateTime == null) {
            gen.writeNull();
            return;
        }

        // Chuyển LocalDateTime thành ISO 8601 format và gửi về cho frontend
        // Frontend sẽ nhận được format: "2025-07-31T19:00:00"
        String isoString = localDateTime.format(ISO_FORMATTER);
        gen.writeString(isoString);
    }
}