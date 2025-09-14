package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.CacheManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for cache management and monitoring
 * Used for administrative purposes
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;

    /**
     * Invalidate analytics cache for a specific URL
     */
    @DeleteMapping("/analytics/url/{urlId}")
    public ResponseEntity<String> invalidateUrlCache(@PathVariable Long urlId) {
        log.info("Manual cache invalidation request for URL: {}", urlId);
        
        try {
            cacheManagementService.invalidateUrlAnalyticsCache(urlId);
            return ResponseEntity.ok("Cache invalidated for URL: " + urlId);
        } catch (Exception e) {
            log.error("Error invalidating cache for URL {}: {}", urlId, e.getMessage());
            return ResponseEntity.status(500).body("Error invalidating cache: " + e.getMessage());
        }
    }

    /**
     * Invalidate all analytics caches
     */
    @DeleteMapping("/analytics/all")
    public ResponseEntity<String> invalidateAllAnalyticsCache() {
        log.info("Manual invalidation of all analytics caches");
        
        try {
            cacheManagementService.invalidateAllAnalyticsCache();
            return ResponseEntity.ok("All analytics caches invalidated");
        } catch (Exception e) {
            log.error("Error invalidating all caches: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error invalidating caches: " + e.getMessage());
        }
    }

    /**
     * Warm up cache
     */
    @PostMapping("/warmup")
    public ResponseEntity<String> warmUpCache() {
        log.info("Manual cache warm-up request");
        
        try {
            cacheManagementService.warmUpCache();
            return ResponseEntity.ok("Cache warm-up completed");
        } catch (Exception e) {
            log.error("Error during cache warm-up: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error during cache warm-up: " + e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getCacheStats() {
        try {
            cacheManagementService.logCacheStatistics();
            return ResponseEntity.ok("Cache statistics logged - check application logs");
        } catch (Exception e) {
            log.error("Error getting cache statistics: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error getting cache statistics: " + e.getMessage());
        }
    }

    /**
     * Invalidate URL ownership cache
     */
    @DeleteMapping("/ownership/{urlId}/{userId}")
    public ResponseEntity<String> invalidateOwnershipCache(@PathVariable Long urlId, @PathVariable Long userId) {
        log.info("Manual ownership cache invalidation for URL: {} and user: {}", urlId, userId);
        
        try {
            cacheManagementService.invalidateUrlOwnershipCache(urlId, userId);
            return ResponseEntity.ok("URL ownership cache invalidated for URL: " + urlId + " and user: " + userId);
        } catch (Exception e) {
            log.error("Error invalidating ownership cache: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error invalidating ownership cache: " + e.getMessage());
        }
    }
}