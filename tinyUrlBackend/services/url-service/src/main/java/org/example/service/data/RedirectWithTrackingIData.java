package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedirectWithTrackingIData {
    private String shortCode;
    private String password; // null if no password required
    private String userId; // null for anonymous users
    private String ipAddress;
    private String userAgent;
    private String referrer; // null if no referrer
}