package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;
import org.example.entity.Url;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllUrlInfoOData {
    private ErrorCode errorCode;
    private String originalUrl;
    private String title;
    private Url.UrlStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;
    private LocalDateTime expiredAt;
}
