package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedirectWithTrackingOData {
    private ErrorCode errorCode;
    private String originalUrl;
    private String shortCode; // for password required case
    private String correlationId; // for tracking the click event
}