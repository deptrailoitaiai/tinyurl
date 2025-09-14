package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for receiving click events from analytics realtime service via HTTP API
 * This will be used when calling realtime service endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEventDto {

    private Long id;
    private String ipAddress;
    private String referrer;
    private Long deviceId;
    private Long locationId;
    private LocalDateTime clickedAt;
    private Boolean processed;
    
    /**
     * URL ID extracted from service_references
     * This links the click event to a specific URL
     */
    private Long urlId;
}