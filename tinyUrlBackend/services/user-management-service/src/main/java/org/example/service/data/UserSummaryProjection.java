package org.example.service.data;

/**
 * Simplified projection for basic user information display
 */
public interface UserSummaryProjection {
    
    Long getId();
    
    String getUsername();
    
    String getFullName();
    
    Boolean getEmailVerified();
    
    org.example.entity.User.UserStatus getStatus();
}
