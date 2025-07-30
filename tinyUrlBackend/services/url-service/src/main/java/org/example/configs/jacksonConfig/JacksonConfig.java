package org.example.configs.jacksonConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.example.util.serializer.UserTimeZoneLocalDateTimeSerializer;
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
                .modules(timeZoneModule())
                .build();
    }

    @Bean
    public SimpleModule timeZoneModule() {
        SimpleModule module = new SimpleModule("TimeZoneModule");

        // TẤT CẢ LocalDateTime sẽ tự động dùng serializer này
        module.addSerializer(LocalDateTime.class, new UserTimeZoneLocalDateTimeSerializer());

        return module;
    }
}
