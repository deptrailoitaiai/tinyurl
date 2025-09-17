package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ClickEventDto;
import org.example.entity.ServiceReference;
import org.example.entity.UrlDailyStats;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlDailyStatsMasterRepository;
import org.example.repository.slave.UrlDailyStatsSlaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core service for batch processing click events into daily statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessingService {

    private final UrlDailyStatsMasterRepository urlDailyStatsMasterRepository;
    private final UrlDailyStatsSlaveRepository urlDailyStatsSlaveRepository;
    private final ServiceReferenceMasterRepository serviceReferenceMasterRepository;
    private final ExternalServiceClient externalServiceClient;
    private final CacheManagementService cacheManagementService;

    /**
     * Process daily statistics for a specific date
     * This method is called by the scheduled job
     */
    @Transactional("masterTransactionManager")
    public void processDailyStats(LocalDate date) {
        log.info("Starting daily statistics processing for date: {}", date);

        try {
            // Fetch click events from realtime service using Kafka
            List<ClickEventDto> clickEvents = externalServiceClient.fetchClickEventsFromRealtimeService(date)
                .get(30, java.util.concurrent.TimeUnit.SECONDS); // Wait max 30 seconds for bulk data
            
            log.info("Fetched {} click events for date {} from realtime service", clickEvents.size(), date);

            // Group events by URL ID
            Map<Long, List<ClickEventDto>> eventsByUrl = clickEvents.stream()
                .collect(java.util.stream.Collectors.groupingBy(ClickEventDto::getUrlId));

            log.info("Found {} URLs with clicks on {}", eventsByUrl.size(), date);

            for (Map.Entry<Long, List<ClickEventDto>> entry : eventsByUrl.entrySet()) {
                Long urlId = entry.getKey();
                List<ClickEventDto> urlEvents = entry.getValue();
                
                try {
                    processUrlDailyStats(urlId, date, urlEvents);
                } catch (Exception e) {
                    log.error("Error processing stats for URL {} on date {}: {}", urlId, date, e.getMessage(), e);
                    // Continue with other URLs even if one fails
                }
            }

            // Clear cache after batch processing
            cacheManagementService.invalidateAllAnalyticsCache();
            log.info("Invalidated analytics cache after batch processing");

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout fetching click events from realtime service for date {}", date, e);
            throw new RuntimeException("Failed to fetch click events - service timeout");
        } catch (Exception e) {
            log.error("Failed to process daily statistics for date {}: {}", date, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process statistics for a specific URL on a specific date with provided click events
     */
    @Transactional("masterTransactionManager")
    public void processUrlDailyStats(Long urlId, LocalDate date, List<ClickEventDto> clickEvents) {
        log.debug("Processing stats for URL {} on date {} with {} events", urlId, date, clickEvents.size());

        // Get or create daily stats record
        UrlDailyStats dailyStats = getOrCreateDailyStats(urlId, date);
        
        // Get last processed click ID
        Long lastProcessedClickId = dailyStats.getLastProcessedClickId();
        
        // Filter events that haven't been processed yet
        List<ClickEventDto> newClicks = clickEvents.stream()
            .filter(event -> lastProcessedClickId == null || event.getId() > lastProcessedClickId)
            .sorted((e1, e2) -> Long.compare(e1.getId(), e2.getId())) // Sort by ID to ensure proper ordering
            .collect(java.util.stream.Collectors.toList());
        
        if (!newClicks.isEmpty()) {
            // Update statistics
            long additionalClicks = newClicks.size();
            Long newLastProcessedId = newClicks.get(newClicks.size() - 1).getId();
            
            urlDailyStatsMasterRepository.updateClickCountAndLastProcessed(
                dailyStats.getId(), 
                additionalClicks, 
                newLastProcessedId
            );
            
            log.debug("Updated stats for URL {}: added {} clicks, last processed ID: {}", 
                     urlId, additionalClicks, newLastProcessedId);
            
            // Invalidate cache for this specific URL
            cacheManagementService.invalidateUrlAnalyticsCache(urlId);
        } else {
            log.debug("No new clicks found for URL {} on date {}", urlId, date);
        }
    }

    /**
     * Get or create daily stats record for URL and date
     */
    private UrlDailyStats getOrCreateDailyStats(Long urlId, LocalDate date) {
        Optional<UrlDailyStats> existingStats = urlDailyStatsMasterRepository.findByDateAndUrlId(date, urlId);
        
        if (existingStats.isPresent()) {
            return existingStats.get();
        }
        
        // Create new daily stats record
        UrlDailyStats newStats = UrlDailyStats.builder()
                .date(date)
                .clickCount(0L)
                .lastProcessedClickId(0L)
                .build();
        
        UrlDailyStats savedStats = urlDailyStatsMasterRepository.save(newStats);
        
        // Create service reference
        ServiceReference serviceRef = ServiceReference.forUrlDailyStats(savedStats.getId(), urlId);
        serviceReferenceMasterRepository.save(serviceRef);
        
        log.debug("Created new daily stats record for URL {} on date {}", urlId, date);
        return savedStats;
    }

    /**
     * Get URLs that have clicks on a specific date
     * TODO: Replace with actual API call to realtime service
     */
    private List<Long> getUrlsWithClicksOnDate(LocalDate date) {
        // STUB IMPLEMENTATION - For now return empty list
        // In real implementation, this would call the realtime service API
        log.warn("STUB: getUrlsWithClicksOnDate - returning empty list. This should call realtime service API.");
        return List.of();
    }

    /**
     * Get new click events for a URL on a specific date after the last processed click ID
     * TODO: Replace with actual API call to realtime service
     */
    private List<ClickEventDto> getNewClickEvents(Long urlId, LocalDate date, Long lastProcessedClickId) {
        // STUB IMPLEMENTATION - For now return empty list
        // In real implementation, this would call the realtime service API
        log.warn("STUB: getNewClickEvents for URL {} - returning empty list. This should call realtime service API.", urlId);
        return List.of();
    }
}