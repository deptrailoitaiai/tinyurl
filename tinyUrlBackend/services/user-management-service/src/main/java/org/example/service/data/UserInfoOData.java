package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoOData {
    private ErrorCode errCode;
    private Long userId;
    private String email;
    private String fullName;
    private LocalDateTime lastUpdate;
    private LocalDateTime lastLoginAt;
}
