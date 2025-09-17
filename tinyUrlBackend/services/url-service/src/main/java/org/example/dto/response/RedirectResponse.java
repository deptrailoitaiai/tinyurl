package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedirectResponse {
    private String originalUrl;
    private boolean passwordRequired;
    private String shortCode;
    private String correlationId; // For tracking click events
}
