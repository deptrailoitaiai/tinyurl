package org.example.service.data;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUrlInfo_I_Data {
    private String originalUrl;
    private String title;
    private String password;
    private String shortCode;
    private LocalDateTime expiresAt;
}
