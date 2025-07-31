package org.example.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.timezone.UserTimeZoneLocalDateTimeDeserializer;
import org.example.timezone.UserTimeZoneLocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule(), timeZoneModule())
                .build();
    }

    @Bean
    public SimpleModule timeZoneModule() {
        SimpleModule module = new SimpleModule("TimeZoneModule");

        // Đăng ký custom serializer/deserializer cho LocalDateTime
        module.addSerializer(LocalDateTime.class, new UserTimeZoneLocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new UserTimeZoneLocalDateTimeDeserializer());

        return module;
    }
}
