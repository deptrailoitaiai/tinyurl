package org.example.service.data;

/**
 * Minimal projection for authentication purposes
 */
public interface UserAuthProjection {
    
    Long getId();
    
    String getUsername();
    
    String getEmail();
    
    String getPasswordHash();
    
    org.example.entity.User.UserStatus getStatus();
    
    Boolean getEmailVerified();
}
