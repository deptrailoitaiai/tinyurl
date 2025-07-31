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
public class CreateUrlResponse {
    private String shortCode;
    private String originalUrl;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
