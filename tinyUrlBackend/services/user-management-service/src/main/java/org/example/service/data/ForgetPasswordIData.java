package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgetPasswordIData {
    private String email;
    private String newPassword;
    private String confirmPassword;
    private String verificationCode;
    private String resetToken;
}
