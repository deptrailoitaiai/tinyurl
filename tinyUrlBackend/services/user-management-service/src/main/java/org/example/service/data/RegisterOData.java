package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterOData {
    private boolean success;
    private String message;
    private Long userId;
    private String verificationToken;
    private boolean emailVerificationRequired;
}
