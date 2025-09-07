package org.example.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private Long userId;
    private String email;
    private String message;
    private boolean emailVerificationRequired;
}
