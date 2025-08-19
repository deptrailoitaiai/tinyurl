package org.example.service.publicKeyProvider;

import org.example.service.data.PublicKeyOData;
import org.springframework.stereotype.Service;

@Service
public class DefaultPublicKeyProviderService implements PublicKeyProviderService {

    @Override
    public PublicKeyOData getCurrentPublicKey() {
        // TODO: Implement get current public key logic
        return null;
    }

    @Override
    public void rotatePublicKey() {
        // TODO: Implement public key rotation logic
    }

    // TODO: Implement cron job for changing public key frequency
    // TODO: Add methods for key generation, rotation, and management
}
