package org.example.service.refreshAccessToken;

import org.example.service.data.RefreshAccessTokenIData;
import org.example.service.data.RefreshAccessTokenOData;

public interface RefreshAccessTokenService {
    RefreshAccessTokenOData refresh(RefreshAccessTokenIData input);
}
