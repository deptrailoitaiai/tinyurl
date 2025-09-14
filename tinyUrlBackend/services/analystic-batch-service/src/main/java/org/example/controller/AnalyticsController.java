package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UrlAnalyticsDto;
import org.example.service.AnalyticsQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for analytics API endpoints
 * Provides URL analytics data to authenticated users
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    /**
     * Get analytics for a specific URL within a date range
     * 
     * @param urlId URL ID to get analytics for
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param userIdHeader User ID from API Gateway
     * @return List of daily analytics data
     */
    @GetMapping("/urls/{urlId}")
    public ResponseEntity<List<UrlAnalyticsDto>> getUrlAnalytics(
            @PathVariable Long urlId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("X-User-Id") Long userIdHeader) {

        log.info("Analytics request for URL {} from {} to {} by user {}", urlId, startDate, endDate, userIdHeader);

        try {
            List<UrlAnalyticsDto> analytics = analyticsQueryService.getUrlAnalytics(urlId, startDate, endDate, userIdHeader);

            return ResponseEntity.ok(analytics);

        } catch (SecurityException e) {
            log.warn("Unauthorized access attempt for URL {} analytics by user {}: {}", urlId, userIdHeader, e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error getting analytics for URL {}: {}", urlId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get latest analytics for a URL (last N days)
     * 
     * @param urlId URL ID to get analytics for
     * @param days Number of days to retrieve (default: 30)
     * @param userIdHeader User ID from API Gateway
     * @return List of recent daily analytics data
     */
    @GetMapping("/urls/{urlId}/latest")
    public ResponseEntity<List<UrlAnalyticsDto>> getLatestUrlAnalytics(
            @PathVariable Long urlId,
            @RequestParam(defaultValue = "30") int days,
            @RequestHeader("X-User-Id") Long userIdHeader) {

        log.info("Latest analytics request for URL {} for {} days by user {}", urlId, days, userIdHeader);

        try {
            List<UrlAnalyticsDto> analytics = analyticsQueryService.getLatestUrlAnalytics(urlId, userIdHeader, days);

            return ResponseEntity.ok(analytics);

        } catch (SecurityException e) {
            log.warn("Unauthorized access attempt for URL {} latest analytics by user {}: {}", urlId, userIdHeader, e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error getting latest analytics for URL {}: {}", urlId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get analytics summary for a URL (total clicks, latest date)
     * 
     * @param urlId URL ID to get summary for
     * @param userIdHeader User ID from API Gateway
     * @return Analytics summary
     */
    @GetMapping("/urls/{urlId}/summary")
    public ResponseEntity<UrlAnalyticsDto> getUrlAnalyticsSummary(
            @PathVariable Long urlId,
            @RequestHeader("X-User-Id") Long userIdHeader) {

        log.info("Analytics summary request for URL {} by user {}", urlId, userIdHeader);

        try {
            UrlAnalyticsDto summary = analyticsQueryService.getUrlAnalyticsSummary(urlId, userIdHeader);

            return ResponseEntity.ok(summary);

        } catch (SecurityException e) {
            log.warn("Unauthorized access attempt for URL {} summary by user {}: {}", urlId, userIdHeader, e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error getting summary for URL {}: {}", urlId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Batch Service is running");
    }
}