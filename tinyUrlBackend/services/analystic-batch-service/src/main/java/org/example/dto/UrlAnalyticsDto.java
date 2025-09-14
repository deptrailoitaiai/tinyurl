package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning URL analytics data to clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlAnalyticsDto {

    private Long urlId;
    private LocalDate date;
    private Long clickCount;
    private LocalDateTime lastProcessedAt;
    
    // Additional analytics data can be added here in the future
    // e.g., unique visitors, referrers, device types, etc.
}