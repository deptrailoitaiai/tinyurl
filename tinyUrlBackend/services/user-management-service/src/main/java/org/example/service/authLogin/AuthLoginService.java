package org.example.service.authLogin;

import org.example.service.data.AuthenticateLoginIData;
import org.example.service.data.AuthenticateLoginOData;

public interface AuthLoginService {
    AuthenticateLoginOData authenticate(AuthenticateLoginIData authenticateLoginIData);
}
