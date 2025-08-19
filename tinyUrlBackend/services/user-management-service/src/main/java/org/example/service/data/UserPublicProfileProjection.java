package org.example.service.data;

import java.time.LocalDateTime;

/**
 * Projection for public user profile information (without sensitive data)
 */
public interface UserPublicProfileProjection {
    
    Long getId();
    
    String getUsername();
    
    String getFullName();
    
    Boolean getEmailVerified();
    
    org.example.entity.User.UserStatus getStatus();
    
    LocalDateTime getCreatedAt();
    
    LocalDateTime getLastLoginAt();
}
