package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlInfoResponse {
    private String shortCode;
    private String originalUrl;
    private String title;
    private String status;
    private boolean hasPassword;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private LocalDateTime expiresAt;
}
