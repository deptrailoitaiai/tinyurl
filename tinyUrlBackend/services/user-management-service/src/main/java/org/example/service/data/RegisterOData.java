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
public class RegisterOData {
    private ErrorCode errCode;
    private Long userId;
    private String email;
    private String verificationToken;
}
