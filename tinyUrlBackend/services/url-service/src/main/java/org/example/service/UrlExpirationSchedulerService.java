package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlExpirationSchedulerService {

    private final UrlExpirationService urlExpirationService;

    /**
     * Scheduled job that runs daily at 00:00 UTC to process expired URLs
     * Cron expression: "0 0 0 * * ?" means:
     * - Second: 0
     * - Minute: 0  
     * - Hour: 0 (midnight)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: ? (any day of week)
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    public void scheduleExpiredUrlsCleanup() {
        ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Starting scheduled URL expiration cleanup job at UTC: {}", startTime);
        
        try {
            // Check count before processing
            int expiredCount = urlExpirationService.getExpiredUrlCount();
            log.info("Found {} URLs to expire in this run", expiredCount);
            
            if (expiredCount > 0) {
                // Process expired URLs
                urlExpirationService.processExpiredUrls();
                log.info("Scheduled URL expiration cleanup completed successfully");
            } else {
                log.info("No expired URLs to process in this run");
            }
            
        } catch (Exception e) {
            log.error("Error occurred during scheduled URL expiration cleanup", e);
            // Note: We don't re-throw here as it would stop the scheduler
            // Instead, we log the error and let the next scheduled run proceed
        }
        
        ZonedDateTime endTime = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Finished scheduled URL expiration cleanup job at UTC: {}, Duration: {} ms", 
                endTime, java.time.Duration.between(startTime, endTime).toMillis());
    }

    /**
     * Optional: Manual trigger for URL expiration cleanup
     * This can be useful for testing or manual execution
     */
    public void manualExpiredUrlsCleanup() {
        log.info("Manual URL expiration cleanup triggered");
        try {
            urlExpirationService.processExpiredUrls();
            log.info("Manual URL expiration cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error occurred during manual URL expiration cleanup", e);
            throw e;
        }
    }
}