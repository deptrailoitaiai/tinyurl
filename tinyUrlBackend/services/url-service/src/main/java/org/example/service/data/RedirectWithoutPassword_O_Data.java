package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedirectWithoutPassword_O_Data {
    boolean urlAvailable;
    boolean needPassword;
    String shortCode;
    String originalUrl;
}