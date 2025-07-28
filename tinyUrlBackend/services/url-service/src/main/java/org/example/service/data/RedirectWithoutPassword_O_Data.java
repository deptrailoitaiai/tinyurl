package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.ErrorCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedirectWithoutPassword_O_Data {
    ErrorCode errorCode;
    String shortCode;
    String originalUrl;
}