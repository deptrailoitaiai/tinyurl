package org.example.dto.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Click event message for Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventMessage {
    
    private String urlId;
    private String shortCode;
    private String userId; // null for anonymous users
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    private String ipAddress;
    private String userAgent;
    private String referrer; // null if no referrer
    private String correlationId;
    
    // Location information
    private LocationInfo location;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private String country;
        private String city;
        private String region;
    }
}