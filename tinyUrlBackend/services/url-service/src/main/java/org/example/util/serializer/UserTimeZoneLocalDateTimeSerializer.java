package org.example.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.example.util.TimeZoneContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserTimeZoneLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(LocalDateTime utcDateTime, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (utcDateTime == null) {
            gen.writeNull();
            return;
        }

        // Tự động convert từ UTC sang timezone của user
        ZoneId userTimeZone = TimeZoneContext.getCurrentTimeZone();

        LocalDateTime userDateTime = utcDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(userTimeZone)
                .toLocalDateTime();

        gen.writeString(userDateTime.format(FORMATTER));
    }
}