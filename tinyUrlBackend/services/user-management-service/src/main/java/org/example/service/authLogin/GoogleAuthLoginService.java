package org.example.service.authLogin;

import org.example.service.data.AuthenticateLoginIData;
import org.example.service.data.AuthenticateLoginOData;
import org.springframework.stereotype.Service;

@Service("GoogleAuthLoginService")
public class GoogleAuthLoginService implements AuthLoginService {

    @Override
    public AuthenticateLoginOData authenticate(AuthenticateLoginIData authenticateLoginIData) {
        // TODO: Implement Google OAuth authentication logic
        // Verify Google token, get user info from Google, create/update user
        return null;
    }
}
