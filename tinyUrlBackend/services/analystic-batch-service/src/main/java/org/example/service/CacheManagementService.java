package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Service for managing cache operations and invalidation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManagementService {

    private final CacheManager cacheManager;

    /**
     * Invalidate analytics cache for a specific URL
     * Called after batch processing updates
     */
    public void invalidateUrlAnalyticsCache(Long urlId) {
        log.info("Invalidating analytics cache for URL: {}", urlId);
        
        try {
            // Invalidate daily stats cache
            Cache dailyStatsCache = cacheManager.getCache("dailyStats");
            if (dailyStatsCache != null) {
                // Clear all entries for this URL (pattern-based eviction)
                dailyStatsCache.clear(); // For simplicity, clear all. In production, use more specific eviction
            }
            
            // Invalidate summary cache
            Cache summaryCache = cacheManager.getCache("analyticsSummary");
            if (summaryCache != null) {
                summaryCache.clear(); // URL-specific eviction would be better
            }
            
            // Invalidate latest analytics cache
            Cache latestCache = cacheManager.getCache("latestAnalytics");
            if (latestCache != null) {
                latestCache.clear();
            }
            
            log.debug("Successfully invalidated analytics cache for URL: {}", urlId);
            
        } catch (Exception e) {
            log.error("Error invalidating cache for URL {}: {}", urlId, e.getMessage(), e);
        }
    }

    /**
     * Invalidate all analytics caches
     * Called after major batch processing operations
     */
    public void invalidateAllAnalyticsCache() {
        log.info("Invalidating all analytics caches");
        
        try {
            String[] cacheNames = {"dailyStats", "analyticsSummary", "latestAnalytics"};
            
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.debug("Cleared cache: {}", cacheName);
                }
            }
            
            log.info("Successfully invalidated all analytics caches");
            
        } catch (Exception e) {
            log.error("Error invalidating all analytics caches: {}", e.getMessage(), e);
        }
    }

    /**
     * Invalidate URL ownership cache for a specific URL and user
     */
    public void invalidateUrlOwnershipCache(Long urlId, Long userId) {
        log.debug("Invalidating URL ownership cache for URL: {} and user: {}", urlId, userId);
        
        try {
            Cache ownershipCache = cacheManager.getCache("urlOwnership");
            if (ownershipCache != null) {
                String cacheKey = urlId + "_" + userId;
                ownershipCache.evict(cacheKey);
                log.debug("Evicted URL ownership cache key: {}", cacheKey);
            }
            
        } catch (Exception e) {
            log.error("Error invalidating URL ownership cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Warm up cache with frequently accessed data
     * Can be called during application startup or scheduled
     */
    public void warmUpCache() {
        log.info("Starting cache warm-up process");
        
        // TODO: Implement cache warm-up logic
        // This could pre-load frequently accessed analytics data
        
        log.info("Cache warm-up process completed");
    }

    /**
     * Get cache statistics for monitoring
     */
    public void logCacheStatistics() {
        log.info("=== Cache Statistics ===");
        
        String[] cacheNames = {"dailyStats", "analyticsSummary", "latestAnalytics", "urlOwnership", "externalService"};
        
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Object nativeCache = cache.getNativeCache();
                log.info("Cache [{}]: Native implementation: {}", cacheName, nativeCache.getClass().getSimpleName());
            }
        }
    }
}