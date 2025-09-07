package org.example.dto.request;

import lombok.Data;

@Data
public class VerifyEmailRequest {
    private String token;
}
