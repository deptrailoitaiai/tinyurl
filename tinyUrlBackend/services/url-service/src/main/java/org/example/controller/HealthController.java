package org.example.controller;

import org.example.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "url-service");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("version", "1.0.0");

        return ResponseEntity.ok(ApiResponse.success("Service is healthy", healthInfo));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put("name", "URL Shortener Service");
        serviceInfo.put("description", "Backend API for URL shortening functionality");
        serviceInfo.put("version", "1.0.0");
        serviceInfo.put("endpoints", new String[] {
                "POST /api/urls - Create short URL",
                "GET /api/urls/{shortCode} - Get URL info",
                "PUT /api/urls/{shortCode} - Update URL",
                "DELETE /api/urls/{shortCode} - Delete URL",
                "GET /api/urls/{shortCode}/redirect - Get redirect URL",
                "POST /api/urls/{shortCode}/redirect - Verify password and get redirect URL"
        });

        return ResponseEntity.ok(ApiResponse.success("Service information", serviceInfo));
    }
}
