package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Url;

/**
 * Unified cache DTO for URL redirect information
 * Contains fields needed for redirect operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlRedirectCacheData {
    private String shortCode;
    private String originalUrl;
    private String passwordHash;
    private Url.UrlStatus status;
    
    /**
     * Create from Url entity
     */
    public static UrlRedirectCacheData fromUrl(Url url, String shortCode) {
        return UrlRedirectCacheData.builder()
                .shortCode(shortCode)
                .originalUrl(url.getOriginalUrl())
                .passwordHash(url.getPasswordHash())
                .status(url.getStatus())
                .build();
    }
    
    /**
     * Check if URL is available for redirect (not expired or disabled)
     */
    public boolean isAvailableForRedirect() {
        return status != Url.UrlStatus.EXPIRED && status != Url.UrlStatus.DISABLED;
    }
    
    /**
     * Check if URL requires password
     */
    public boolean isPasswordProtected() {
        return passwordHash != null;
    }
}
