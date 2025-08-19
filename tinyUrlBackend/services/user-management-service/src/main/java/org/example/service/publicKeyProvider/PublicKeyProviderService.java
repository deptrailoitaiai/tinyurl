package org.example.service.publicKeyProvider;

import org.example.service.data.PublicKeyOData;

public interface PublicKeyProviderService {
    PublicKeyOData getCurrentPublicKey();
    
    void rotatePublicKey();
    
    // cron (change public key frequency)
}
