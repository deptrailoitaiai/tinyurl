package org.example.service.authLogin;

import org.example.service.data.AuthenticateLoginIData;
import org.example.service.data.AuthenticateLoginOData;
import org.springframework.stereotype.Service;

@Service("passwordAuthLoginService")
public class PasswordAuthLoginService implements AuthLoginService {

    @Override
    public AuthenticateLoginOData authenticate(AuthenticateLoginIData authenticateLoginIData) {
        // TODO: Implement password-based authentication logic
        // Verify email and password, generate JWT tokens
        return null;
    }
}
