package org.example.healthChecker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthCheckResponse {
    private String status;
    private String timestamp;
    private String applicationName;
    private String version;
    private long uptime;
    private String message;
    private SystemInfo systemInfo;
}