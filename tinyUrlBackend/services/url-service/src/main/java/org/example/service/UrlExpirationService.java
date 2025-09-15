package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Url;
import org.example.repository.master.UrlMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlExpirationService {

    private final UrlMasterRepository urlMasterRepository;

    /**
     * Process all expired URLs and update their status to EXPIRED
     * This method runs in UTC timezone
     */
    @Transactional
    public void processExpiredUrls() {
        LocalDateTime currentUtcTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        
        log.info("Starting URL expiration process at UTC time: {}", currentUtcTime);
        
        try {
            // Find all expired URLs that are still active
            List<Url> expiredUrls = urlMasterRepository.findExpiredActiveUrls(currentUtcTime);
            
            if (expiredUrls.isEmpty()) {
                log.info("No expired URLs found");
                return;
            }
            
            log.info("Found {} expired URLs to process", expiredUrls.size());
            
            // Update status of expired URLs in batch
            int updatedCount = urlMasterRepository.updateExpiredUrlsStatus(currentUtcTime, currentUtcTime);
            
            log.info("Successfully updated {} URLs from ACTIVE to EXPIRED status", updatedCount);
            
            // Log details of processed URLs (optional, for debugging)
            if (log.isDebugEnabled()) {
                expiredUrls.forEach(url -> 
                    log.debug("Expired URL processed - ID: {}, Original URL: {}, Expired At: {}", 
                        url.getId(), url.getOriginalUrl(), url.getExpiresAt())
                );
            }
            
        } catch (Exception e) {
            log.error("Error occurred while processing expired URLs", e);
            throw e; // Re-throw to ensure transaction rollback
        }
    }

    /**
     * Get count of expired URLs that need to be processed
     */
    @Transactional(readOnly = true)
    public int getExpiredUrlCount() {
        LocalDateTime currentUtcTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        List<Url> expiredUrls = urlMasterRepository.findExpiredActiveUrls(currentUtcTime);
        return expiredUrls.size();
    }
}