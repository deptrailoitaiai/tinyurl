package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UrlAnalyticsDto;
import org.example.entity.UrlDailyStats;
import org.example.repository.slave.ServiceReferenceSlaveRepository;
import org.example.repository.slave.UrlDailyStatsSlaveRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for providing analytics data to API consumers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final UrlDailyStatsSlaveRepository urlDailyStatsSlaveRepository;
    private final ServiceReferenceSlaveRepository serviceReferenceSlaveRepository;
    private final ExternalServiceClient externalServiceClient;

        /**
     * Get analytics for a URL within a date range
     * Verifies URL ownership before returning data
     */
    @Cacheable(value = "dailyStats", key = "#urlId + '_' + #startDate + '_' + #endDate + '_' + #userId")
    @Transactional(value = "slaveTransactionManager", readOnly = true)
    public List<UrlAnalyticsDto> getUrlAnalytics(Long urlId, LocalDate startDate, LocalDate endDate, Long userId) {
        log.debug("Getting analytics for URL {} from {} to {} for user {}", urlId, startDate, endDate, userId);

        try {
            // Verify URL ownership using async Kafka request
            boolean isOwner = externalServiceClient.verifyUrlOwnership(String.valueOf(urlId), String.valueOf(userId))
                .get(10, java.util.concurrent.TimeUnit.SECONDS); // Wait max 10 seconds
            
            if (!isOwner) {
                throw new SecurityException("User does not own this URL");
            }
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout verifying URL ownership for URL {} and user {}", urlId, userId);
            throw new RuntimeException("Unable to verify URL ownership - service timeout");
        } catch (Exception e) {
            log.error("Error verifying URL ownership for URL {} and user {}", urlId, userId, e);
            throw new RuntimeException("Unable to verify URL ownership");
        }

        // Check if URL has any analytics data
        if (!serviceReferenceSlaveRepository.existsByUrlId(urlId)) {
            log.debug("No analytics data found for URL {}", urlId);
            return List.of();
        }

        // Get daily stats for the date range
        List<UrlDailyStats> dailyStats = urlDailyStatsSlaveRepository.findByUrlIdAndDateRange(urlId, startDate, endDate);

        return dailyStats.stream()
                .map(stats -> UrlAnalyticsDto.builder()
                        .urlId(urlId)
                        .date(stats.getDate())
                        .clickCount(stats.getClickCount())
                        .lastProcessedAt(stats.getLastProcessedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get latest analytics for a URL (last N days)
     */
    @Cacheable(value = "latestAnalytics", key = "#urlId + '_' + #userId + '_' + #days")
    @Transactional(value = "slaveTransactionManager", readOnly = true)
    public List<UrlAnalyticsDto> getLatestUrlAnalytics(Long urlId, Long userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        return getUrlAnalytics(urlId, startDate, endDate, userId);
    }

    /**
     * Get analytics summary for a URL (total clicks, etc.)
     */
    @Cacheable(value = "analyticsSummary", key = "#urlId + '_' + #userId")
    @Transactional(value = "slaveTransactionManager", readOnly = true)
    public UrlAnalyticsDto getUrlAnalyticsSummary(Long urlId, Long userId) {
        log.debug("Getting analytics summary for URL {} for user {}", urlId, userId);

        try {
            // Verify URL ownership using async Kafka request
            boolean isOwner = externalServiceClient.verifyUrlOwnership(String.valueOf(urlId), String.valueOf(userId))
                .get(10, java.util.concurrent.TimeUnit.SECONDS); // Wait max 10 seconds
            
            if (!isOwner) {
                throw new SecurityException("User does not own this URL");
            }
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout verifying URL ownership for URL {} and user {}", urlId, userId);
            throw new RuntimeException("Unable to verify URL ownership - service timeout");
        } catch (Exception e) {
            log.error("Error verifying URL ownership for URL {} and user {}", urlId, userId, e);
            throw new RuntimeException("Unable to verify URL ownership");
        }

        // Get all daily stats for this URL
        List<UrlDailyStats> allStats = urlDailyStatsSlaveRepository.findByUrlIdOrderByDateDesc(urlId);

        if (allStats.isEmpty()) {
            return UrlAnalyticsDto.builder()
                    .urlId(urlId)
                    .clickCount(0L)
                    .build();
        }

        // Calculate total clicks
        long totalClicks = allStats.stream()
                .mapToLong(UrlDailyStats::getClickCount)
                .sum();

        // Get latest update time
        LocalDate latestDate = allStats.get(0).getDate();
        UrlDailyStats latestStats = allStats.get(0);

        return UrlAnalyticsDto.builder()
                .urlId(urlId)
                .date(latestDate)
                .clickCount(totalClicks)
                .lastProcessedAt(latestStats.getLastProcessedAt())
                .build();
    }
}