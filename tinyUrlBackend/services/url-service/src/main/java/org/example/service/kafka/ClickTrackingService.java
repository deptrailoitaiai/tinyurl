package org.example.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.kafka.ClickEventMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for tracking URL click events via Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickTrackingService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.click-events:url.click.events}")
    private String clickEventsTopic;
    
    /**
     * Track a URL click event asynchronously
     * 
     * @param urlId URL ID
     * @param shortCode Short code
     * @param userId User ID (can be null for anonymous)
     * @param ipAddress User's IP address
     * @param userAgent User agent string
     * @param referrer Referrer URL (can be null)
     * @return correlation ID for tracking
     */
    public String trackClick(String urlId, String shortCode, String userId, 
                           String ipAddress, String userAgent, String referrer) {
        
        String correlationId = UUID.randomUUID().toString();
        
        try {
            ClickEventMessage clickEvent = ClickEventMessage.builder()
                    .urlId(urlId)
                    .shortCode(shortCode)
                    .userId(userId)
                    .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .correlationId(correlationId)
                    .location(extractLocationFromIP(ipAddress)) // Simple implementation
                    .build();
            
            // Send asynchronously with callback
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(clickEventsTopic, correlationId, clickEvent);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.debug("Click event sent successfully: correlationId={}, urlId={}", 
                             correlationId, urlId);
                } else {
                    log.error("Failed to send click event: correlationId={}, urlId={}, error={}", 
                             correlationId, urlId, exception.getMessage(), exception);
                }
            });
            
            log.info("Click event queued for sending: urlId={}, userId={}, correlationId={}", 
                    urlId, userId, correlationId);
            
        } catch (Exception e) {
            log.error("Error creating click event: urlId={}, error={}", urlId, e.getMessage(), e);
        }
        
        return correlationId;
    }
    
    /**
     * Simple location extraction from IP
     * In production, this should use a proper GeoIP service
     */
    private ClickEventMessage.LocationInfo extractLocationFromIP(String ipAddress) {
        // Simple implementation - in production use MaxMind GeoIP2 or similar
        return ClickEventMessage.LocationInfo.builder()
                .country("Unknown")
                .city("Unknown")
                .region("Unknown")
                .build();
    }
}