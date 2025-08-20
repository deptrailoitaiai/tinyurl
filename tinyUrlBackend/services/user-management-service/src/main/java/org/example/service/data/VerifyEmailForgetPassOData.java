package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyEmailForgetPassOData {
    private boolean success;
    private String message;
    private boolean emailVerified;
    private Long userId;
}
