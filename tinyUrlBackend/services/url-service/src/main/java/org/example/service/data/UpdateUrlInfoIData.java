package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Url;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUrlInfoIData {
    private Long userId;
    private String shortCode;
    private String title;
    private String password;
    private Url.UrlStatus status;
    private LocalDateTime expiredAt;
}
