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
public class UpdateUrlInfo_I_Data {
    String shortCode;
    String title;
    String password;
    String status;
    LocalDateTime expiredAt;
}
