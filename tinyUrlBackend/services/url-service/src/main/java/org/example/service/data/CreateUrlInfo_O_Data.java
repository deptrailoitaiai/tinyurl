package org.example.service.data;

import lombok.*;
import org.example.constants.ErrorCode;
import org.example.entity.Url;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUrlInfo_O_Data {
    private ErrorCode errorCode;
    private String shortCode;
    private String originalUrl;
    private Url.UrlStatus status;
    private String title;
    private LocalDateTime createAt;
    private LocalDateTime expiredAt;
}
