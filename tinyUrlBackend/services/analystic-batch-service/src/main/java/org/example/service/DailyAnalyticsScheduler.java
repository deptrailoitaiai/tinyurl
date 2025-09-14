package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Scheduled job for processing daily analytics
 * Runs every day at midnight UTC
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "analytics.batch.enabled", havingValue = "true", matchIfMissing = true)
public class DailyAnalyticsScheduler {

    private final BatchProcessingService batchProcessingService;

    /**
     * Process yesterday's analytics data
     * Runs daily at midnight UTC (cron: 0 0 0 * * *)
     */
    @Scheduled(cron = "${analytics.batch.cron:0 0 0 * * *}")
    public void processYesterdayAnalytics() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        
        log.info("=== Starting scheduled daily analytics processing for {} ===", yesterday);
        
        try {
            long startTime = System.currentTimeMillis();
            
            batchProcessingService.processDailyStats(yesterday);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Completed daily analytics processing for {} in {}ms ===", yesterday, duration);
            
        } catch (Exception e) {
            log.error("=== Failed daily analytics processing for {}: {} ===", yesterday, e.getMessage(), e);
            
            // You might want to send alerts, retry logic, etc.
            // For now, just log the error
        }
    }

    /**
     * Manual trigger for processing a specific date
     * This can be called via JMX or other management interface
     */
    public void processSpecificDate(LocalDate date) {
        log.info("=== Starting manual analytics processing for {} ===", date);
        
        try {
            long startTime = System.currentTimeMillis();
            
            batchProcessingService.processDailyStats(date);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Completed manual analytics processing for {} in {}ms ===", date, duration);
            
        } catch (Exception e) {
            log.error("=== Failed manual analytics processing for {}: {} ===", date, e.getMessage(), e);
            throw e;
        }
    }
}