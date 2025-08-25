package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticateLoginOData {
    private ErrorCode errCode;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String userEmail;
    private Long expiresIn;
}
