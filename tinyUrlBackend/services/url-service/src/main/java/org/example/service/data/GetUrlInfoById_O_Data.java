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
public class GetUrlInfoById_O_Data {
    boolean urlFound;
    String originalUrl;
    String title;
    Url.UrlStatus status;
    LocalDateTime createdAt;
    LocalDateTime lastUpdate;
    LocalDateTime expiredAt;
}
