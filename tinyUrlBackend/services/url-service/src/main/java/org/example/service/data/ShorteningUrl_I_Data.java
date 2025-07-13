package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShorteningUrl_I_Data {
    @NotNull(message = "User ID cannot be null")
    Long userId;
    
    @NotBlank(message = "Original URL cannot be blank")
    @Size(max = 2048, message = "URL too long")
    String originalUrl;
    
    @Size(max = 500, message = "Title too long")
    String title;
    
    @Size(max = 255, message = "Password too long")
    String password;
    
    LocalDateTime expiredDate;
}
