package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ClickEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Client for communicating with external services (realtime service, URL service)
 * Now uses Kafka for inter-service communication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalServiceClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // For tracking pending requests (simple in-memory store)
    private final ConcurrentHashMap<String, CompletableFuture<Boolean>> pendingOwnershipRequests = new ConcurrentHashMap<>();

    @Value("${kafka.topics.url-ownership-requests:url.ownership.requests}")
    private String urlOwnershipRequestsTopic;
    
    @Value("${kafka.topics.url-ownership-responses:url.ownership.responses}")
    private String urlOwnershipResponsesTopic;
    
    @Value("${kafka.topics.analytics-data-requests:analytics.data.requests}")
    private String analyticsDataRequestsTopic;
    
    @Value("${services.request-timeout:10}")
    private long requestTimeoutSeconds;

    /**
     * Get URLs that have click events on a specific date from realtime service
     * TODO: Implement when realtime service API is ready
     */
    public List<Long> getUrlsWithClicksOnDate(LocalDate date) {
        log.warn("STUB: getUrlsWithClicksOnDate for date {} - realtime service not yet implemented", date);
        
        // TODO: Implement actual HTTP call
        // WebClient client = webClientBuilder.baseUrl(realtimeServiceBaseUrl).build();
        // return client.get()
        //     .uri("/api/analytics/urls-with-clicks?date={date}", date)
        //     .retrieve()
        //     .bodyToFlux(Long.class)
        //     .collectList()
        //     .block();
        
        return List.of();
    }

    /**
     * Get new click events for a URL after a specific click ID from realtime service
     * TODO: Implement when realtime service API is ready
     */
    public List<ClickEventDto> getNewClickEvents(Long urlId, LocalDate date, Long afterClickId) {
        log.warn("STUB: getNewClickEvents for URL {} after click ID {} - realtime service not yet implemented", 
                urlId, afterClickId);
        
        // TODO: Implement actual HTTP call
        // WebClient client = webClientBuilder.baseUrl(realtimeServiceBaseUrl).build();
        // return client.get()
        //     .uri("/api/analytics/click-events?urlId={urlId}&date={date}&afterId={afterId}", 
        //          urlId, date, afterClickId)
        //     .retrieve()
        //     .bodyToFlux(ClickEventDto.class)
        //     .collectList()
        //     .block();
        
        return List.of();
    }

        /**
     * Verify if a user owns a specific URL using Kafka request-reply pattern
     */
    @Cacheable(value = "urlOwnership", key = "#urlId + '_' + #userId")
    public CompletableFuture<Boolean> verifyUrlOwnership(String urlId, String userId) {
        String correlationId = UUID.randomUUID().toString();
        log.info("Verifying URL ownership - urlId: {}, userId: {}, correlationId: {}", urlId, userId, correlationId);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // Store pending request
        pendingOwnershipRequests.put(correlationId, future);
        
        // Set timeout
        CompletableFuture.delayedExecutor(requestTimeoutSeconds, TimeUnit.SECONDS)
            .execute(() -> {
                CompletableFuture<Boolean> pendingFuture = pendingOwnershipRequests.remove(correlationId);
                if (pendingFuture != null && !pendingFuture.isDone()) {
                    log.warn("URL ownership verification timeout - correlationId: {}", correlationId);
                    pendingFuture.completeExceptionally(new RuntimeException("Request timeout"));
                }
            });

        try {
            // Build ownership verification request
            var ownershipRequest = OwnershipVerificationRequest.builder()
                .correlationId(correlationId)
                .urlId(urlId)
                .userId(userId)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();

            // Send request to URL service
            kafkaTemplate.send(
                MessageBuilder.withPayload(ownershipRequest)
                    .setHeader(KafkaHeaders.TOPIC, urlOwnershipRequestsTopic)
                    .setHeader(KafkaHeaders.KEY, urlId)
                    .setHeader("correlationId", correlationId)
                    .build()
            );

            log.debug("Sent URL ownership verification request - correlationId: {}", correlationId);
            
        } catch (Exception e) {
            log.error("Failed to send URL ownership verification request - correlationId: {}", correlationId, e);
            pendingOwnershipRequests.remove(correlationId);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Fetch click events from realtime service for a specific date using Kafka request-reply pattern
     */
    public CompletableFuture<List<ClickEventDto>> fetchClickEventsFromRealtimeService(LocalDate date) {
        String correlationId = UUID.randomUUID().toString();
        log.info("Fetching click events from realtime service - date: {}, correlationId: {}", date, correlationId);

        CompletableFuture<List<ClickEventDto>> future = new CompletableFuture<>();
        
        // TODO: Implement response handling for analytics data requests
        // For now, return empty list
        future.complete(List.of());

        try {
            // Build analytics data request
            var analyticsRequest = AnalyticsDataRequest.builder()
                .correlationId(correlationId)
                .requestType("DAILY_CLICK_EVENTS")
                .date(date)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();

            // Send request to realtime service
            kafkaTemplate.send(
                MessageBuilder.withPayload(analyticsRequest)
                    .setHeader(KafkaHeaders.TOPIC, analyticsDataRequestsTopic)
                    .setHeader(KafkaHeaders.KEY, date.toString())
                    .setHeader("correlationId", correlationId)
                    .build()
            );

            log.debug("Sent analytics data request - correlationId: {}", correlationId);
            
        } catch (Exception e) {
            log.error("Failed to send analytics data request - correlationId: {}", correlationId, e);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Handle ownership verification response from URL service
     * Called by Kafka listener
     */
    public void handleOwnershipVerificationResponse(String correlationId, boolean isOwner) {
        log.debug("Received ownership verification response - correlationId: {}, isOwner: {}", correlationId, isOwner);
        
        CompletableFuture<Boolean> pendingFuture = pendingOwnershipRequests.remove(correlationId);
        if (pendingFuture != null) {
            pendingFuture.complete(isOwner);
        } else {
            log.warn("No pending request found for ownership verification response - correlationId: {}", correlationId);
        }
    }

    /**
     * Get URL information from URL service
     * TODO: Implement when URL service API is ready
     */
    @Cacheable(value = "externalService", key = "'urlExists_' + #urlId")
    public boolean urlExists(Long urlId) {
        log.warn("STUB: urlExists for URL {} - URL service not yet implemented", urlId);
        
        // TODO: Implement actual HTTP call
        // WebClient client = webClientBuilder.baseUrl(urlServiceBaseUrl).build();
        // return client.get()
        //     .uri("/api/urls/{urlId}/exists", urlId)
        //     .retrieve()
        //     .bodyToMono(Boolean.class)
        //     .block();
        
        // For now, return true to allow testing
        return true;
    }

    // Inner classes for request/response DTOs
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OwnershipVerificationRequest {
        private String correlationId;
        private String urlId;
        private String userId;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyticsDataRequest {
        private String correlationId;
        private String requestType;
        private LocalDate date;
        private LocalDateTime timestamp;
    }
}