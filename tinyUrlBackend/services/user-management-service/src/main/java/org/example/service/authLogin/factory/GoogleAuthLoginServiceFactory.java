package org.example.service.authLogin.factory;

import org.example.service.authLogin.AuthLoginService;
import org.example.service.authLogin.GoogleAuthLoginService;
import org.springframework.stereotype.Component;

@Component("GoogleAuthLoginServiceFactory")
public class GoogleAuthLoginServiceFactory extends AuthLoginServiceFactory {
    @Override
    public AuthLoginService getAuthService() {
        return new GoogleAuthLoginService();
    }
}
