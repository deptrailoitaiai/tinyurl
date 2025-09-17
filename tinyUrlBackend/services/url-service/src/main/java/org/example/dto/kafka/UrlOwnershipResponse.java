package org.example.dto.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * URL Ownership verification response message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlOwnershipResponse {
    
    private String correlationId;
    private String urlId;
    private String userId;
    private Boolean isOwner;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    private String error;
}