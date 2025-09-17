package org.example.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.ExternalServiceClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Kafka listener for handling responses from other services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaResponseListener {

    private final ExternalServiceClient externalServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Handle ownership verification responses from URL service
     */
    @KafkaListener(
        topics = "${kafka.topics.url-ownership-responses:url.ownership.responses}",
        groupId = "${kafka.consumer.group-id:analytics-batch-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOwnershipVerificationResponse(
            @Payload String messagePayload,
            @Header(KafkaHeaders.RECEIVED_KEY) String urlId,
            @Header(value = "correlationId", required = false) String correlationId) {
        
        try {
            log.debug("Received ownership verification response - urlId: {}, correlationId: {}", urlId, correlationId);
            
            OwnershipVerificationResponse response = objectMapper.readValue(messagePayload, OwnershipVerificationResponse.class);
            
            if (correlationId == null) {
                correlationId = response.getCorrelationId();
            }
            
            // Validate response
            if (correlationId == null || correlationId.trim().isEmpty()) {
                log.error("Missing correlationId in ownership verification response");
                return;
            }
            
            // Forward to external service client for handling
            externalServiceClient.handleOwnershipVerificationResponse(correlationId, response.isOwner());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ownership verification response: {}", messagePayload, e);
        } catch (Exception e) {
            log.error("Error handling ownership verification response", e);
        }
    }

    /**
     * Handle analytics data responses from realtime service
     * TODO: Implement when realtime service is ready
     */
    @KafkaListener(
        topics = "${kafka.topics.analytics-data-responses:analytics.data.responses}",
        groupId = "${kafka.consumer.group-id:analytics-batch-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAnalyticsDataResponse(
            @Payload String messagePayload,
            @Header(KafkaHeaders.RECEIVED_KEY) String requestKey,
            @Header(value = "correlationId", required = false) String correlationId) {
        
        try {
            log.debug("Received analytics data response - key: {}, correlationId: {}", requestKey, correlationId);
            
            // TODO: Parse and handle analytics data response
            log.info("Analytics data response handling not yet implemented");
            
        } catch (Exception e) {
            log.error("Error handling analytics data response", e);
        }
    }

    // Response DTOs

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class OwnershipVerificationResponse {
        private String correlationId;
        private String urlId;
        private String userId;
        private boolean isOwner;
        private String errorMessage;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class AnalyticsDataResponse {
        private String correlationId;
        private String requestType;
        private Object data; // Will be List<ClickEventDto> for click events
        private String errorMessage;
        private LocalDateTime timestamp;
    }
}