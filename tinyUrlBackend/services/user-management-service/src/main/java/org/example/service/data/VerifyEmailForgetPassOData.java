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
public class VerifyEmailForgetPassOData {
    private ErrorCode errCode;
    private String token;
    private boolean success;
    private String message;
    private boolean emailVerified;
    private Long userId;
}
