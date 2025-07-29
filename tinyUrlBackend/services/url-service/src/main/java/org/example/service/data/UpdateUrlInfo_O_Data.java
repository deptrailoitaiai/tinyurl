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
public class UpdateUrlInfo_O_Data {
    ErrorCode errorCode;
    String shortCode;
    String originalUrl;
    String title;
    Url.UrlStatus status;
    LocalDateTime updateAt;
    LocalDateTime expireAt;
}
