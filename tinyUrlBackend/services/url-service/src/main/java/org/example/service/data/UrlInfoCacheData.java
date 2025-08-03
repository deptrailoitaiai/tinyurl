package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Url;

import java.time.LocalDateTime;

/**
 * Unified cache DTO for URL information
 * Contains all fields that might be needed across different operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlInfoCacheData {
    private String shortCode;
    private String originalUrl;
    private String title;
    private String passwordHash;
    private Url.UrlStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    
    /**
     * Convert to GetUrlInfoByIdOData
     */
    public GetUrlInfoByIdOData toGetUrlInfoByIdOData() {
        return GetUrlInfoByIdOData.builder()
                .originalUrl(this.originalUrl)
                .title(this.title)
                .passwordHash(this.passwordHash)
                .status(this.status)
                .createdAt(this.createdAt)
                .lastUpdate(this.updatedAt)
                .expiredAt(this.expiresAt)
                .build();
    }
    
    /**
     * Create from Url entity
     */
    public static UrlInfoCacheData fromUrl(Url url, String shortCode) {
        return UrlInfoCacheData.builder()
                .shortCode(shortCode)
                .originalUrl(url.getOriginalUrl())
                .title(url.getTitle())
                .passwordHash(url.getPasswordHash())
                .status(url.getStatus())
                .createdAt(url.getCreatedAt())
                .updatedAt(url.getUpdatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }
}
