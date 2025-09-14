package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ClickEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

/**
 * Client for communicating with external services (realtime service, URL service)
 * Currently contains stub implementations that will be replaced when other services are ready
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.realtime-service.base-url:http://localhost:8082}")
    private String realtimeServiceBaseUrl;

    @Value("${services.url-service.base-url:http://localhost:8081}")
    private String urlServiceBaseUrl;

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
     * Verify URL ownership with URL service
     * TODO: Implement when URL service API is ready
     */
    @Cacheable(value = "urlOwnership", key = "#urlId + '_' + #userId")
    public boolean verifyUrlOwnership(Long urlId, Long userId) {
        log.warn("STUB: verifyUrlOwnership for URL {} and user {} - URL service not yet implemented", 
                urlId, userId);
        
        // TODO: Implement actual HTTP call
        // WebClient client = webClientBuilder.baseUrl(urlServiceBaseUrl).build();
        // return client.get()
        //     .uri("/api/urls/{urlId}/verify-ownership?userId={userId}", urlId, userId)
        //     .retrieve()
        //     .bodyToMono(Boolean.class)
        //     .block();
        
        // For now, return true to allow testing
        return true;
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
}