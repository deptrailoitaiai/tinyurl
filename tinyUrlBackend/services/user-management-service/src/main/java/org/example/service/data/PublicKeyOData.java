package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicKeyOData {
    private String publicKey;
    private String keyId;
    private Long expiresAt;
    private String algorithm;
}
