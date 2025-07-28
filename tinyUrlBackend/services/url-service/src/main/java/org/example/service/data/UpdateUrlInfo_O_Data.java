package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUrlInfo_O_Data {
    ErrorCode errorCode;
    String shortCode;
    String title;
    String status;
    LocalDateTime updateAt;
    LocalDateTime expireAt;
}
