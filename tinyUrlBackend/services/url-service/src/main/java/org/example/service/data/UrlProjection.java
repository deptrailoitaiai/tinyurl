package org.example.service.data;

import org.example.entity.Url;

import java.time.LocalDateTime;

public interface UrlProjection {
    Long getId();
    String getOriginalUrl();
    String getTitle();
    Url.UrlStatus getStatus();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    LocalDateTime getExpiresAt();
}
