package org.example.service.data;

import org.example.entity.User;

import java.time.LocalDateTime;

public class UserInfoCacheData {
    private String email;
    private String passwordHash;
    private String fullName;
    private Boolean emailVerified;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
