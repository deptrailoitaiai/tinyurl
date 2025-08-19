package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticateLoginOData {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String userEmail;
    private Long expiresIn;
}
