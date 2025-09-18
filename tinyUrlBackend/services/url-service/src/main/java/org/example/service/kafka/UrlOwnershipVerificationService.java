package org.example.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.kafka.UrlOwnershipRequest;
import org.example.dto.kafka.UrlOwnershipResponse;
import org.example.service.UrlManagement.UrlManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Service for handling URL ownership verification requests via Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlOwnershipVerificationService {
    
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Qualifier("DefaultUrlManagementService")
    private UrlManagementService urlManagementService;
    
    @Value("${kafka.topics.url-ownership-responses:url.ownership.responses}")
    private String ownershipResponsesTopic;
    
    /**
     * Listen for URL ownership verification requests
     */
    @KafkaListener(
        topics = "${kafka.topics.url-ownership-requests:url.ownership.requests}",
        groupId = "url-service-ownership-group"
    )
    public void handleOwnershipVerificationRequest(UrlOwnershipRequest request) {
        log.info("Received ownership verification request: correlationId={}, urlId={}, userId={}", 
                request.getCorrelationId(), request.getUrlId(), request.getUserId());
        
        UrlOwnershipResponse response = UrlOwnershipResponse.builder()
                .correlationId(request.getCorrelationId())
                .urlId(request.getUrlId())
                .userId(request.getUserId())
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        
        try {
            // Verify ownership using existing service
            Long urlId = Long.parseLong(request.getUrlId());
            Long userId = Long.parseLong(request.getUserId());
            
            boolean isOwner = isUserAuthorizedForUrl(urlId, userId);
            response.setIsOwner(isOwner);
            
            log.debug("Ownership verification result: urlId={}, userId={}, isOwner={}", 
                     urlId, userId, isOwner);
            
        } catch (NumberFormatException e) {
            log.error("Invalid ID format in ownership request: urlId={}, userId={}", 
                     request.getUrlId(), request.getUserId());
            response.setIsOwner(false);
            response.setError("Invalid ID format");
        } catch (Exception e) {
            log.error("Error verifying ownership: correlationId={}, error={}", 
                     request.getCorrelationId(), e.getMessage(), e);
            response.setIsOwner(false);
            response.setError("Internal error: " + e.getMessage());
        }
        
        // Send response to reply topic or specific topic
        String responseTopic = request.getReplyTo() != null ? request.getReplyTo() : ownershipResponsesTopic;
        
        kafkaTemplate.send(responseTopic, request.getCorrelationId(), response)
            .whenComplete((result, exception) -> {
                if (exception == null) {
                    log.debug("Ownership verification response sent: correlationId={}, isOwner={}", 
                             request.getCorrelationId(), response.getIsOwner());
                } else {
                    log.error("Failed to send ownership verification response: correlationId={}, error={}", 
                             request.getCorrelationId(), exception.getMessage(), exception);
                }
            });
    }
    
    /**
     * Check if a specific user has access to a specific URL
     * This method should use the same logic as in DefaultUrlManagementService
     */
    private boolean isUserAuthorizedForUrl(Long urlId, Long userId) {
        try {
            // For now, we'll use a simple approach
            // In a real implementation, you might want to inject ServiceReferenceRepository directly
            // or call a shared service method
            
            // This is a simplified version - you should implement the actual logic
            // based on your ServiceReference table structure
            log.debug("Verifying URL ownership: urlId={}, userId={}", urlId, userId);
            
            // TODO: Implement actual ownership verification
            // For now returning true to allow testing - replace with actual logic
            return true;
            
        } catch (Exception e) {
            log.error("Error checking URL ownership: urlId={}, userId={}, error={}", 
                     urlId, userId, e.getMessage(), e);
            return false;
        }
    }
}