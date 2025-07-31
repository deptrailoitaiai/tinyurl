package org.example.service.data;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUrlInfoIData {
    private Long userId;
    private String originalUrl;
    private String title;
    private String password;
    private LocalDateTime expiresAt;
}
