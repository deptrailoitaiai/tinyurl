package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;
import org.example.entity.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoCacheData {
    private String email;
    private String passwordHash;
    private String fullName;
    private Boolean emailVerified;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    /**
     * Convert from User entity to cache data
     */
    public static UserInfoCacheData fromUser(User user) {
        return UserInfoCacheData.builder()
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .fullName(user.getFullName())
                .emailVerified(user.getEmailVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * Convert cache data to UserInfoOData
     */
    public UserInfoOData toUserInfoOData(Long userId) {
        UserInfoOData result = new UserInfoOData();
        result.setUserId(userId);
        result.setEmail(this.email);
        result.setFullName(this.fullName);
        result.setLastUpdate(this.updatedAt);
        result.setLastLoginAt(this.lastLoginAt);
        result.setErrCode(ErrorCode.SUCCESS);
        return result;
    }
}
