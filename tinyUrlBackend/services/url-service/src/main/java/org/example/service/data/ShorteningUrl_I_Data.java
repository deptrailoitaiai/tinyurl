package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShorteningUrl_I_Data {
    Long userId;
    String originalUrl;
    String title;
    String password;
    LocalDateTime expiredDate;
}
