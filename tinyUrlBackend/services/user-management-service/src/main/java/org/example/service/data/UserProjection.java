package org.example.service.data;

import org.example.entity.User;

import java.time.LocalDateTime;

/**
 * Projection interface for User entity to optimize read operations
 */
public interface UserProjection {
    
    Long getId();
    
    String getUsername();
    
    String getEmail();
    
    String getFullName();
    
    Boolean getEmailVerified();
    
    User.UserStatus getStatus();
    
    LocalDateTime getCreatedAt();
    
    LocalDateTime getUpdatedAt();
    
    LocalDateTime getLastLoginAt();
}
