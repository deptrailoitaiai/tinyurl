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
public class GetUrlInfoByIdOData {
    ErrorCode errorCode;
    String originalUrl;
    String title;
    String passwordHash;
    Url.UrlStatus status;
    LocalDateTime createdAt;
    LocalDateTime lastUpdate;
    LocalDateTime expiredAt;
}
