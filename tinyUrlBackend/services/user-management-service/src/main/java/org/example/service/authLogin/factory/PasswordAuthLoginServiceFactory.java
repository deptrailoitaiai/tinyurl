package org.example.service.authLogin.factory;

import org.example.service.authLogin.AuthLoginService;
import org.example.service.authLogin.PasswordAuthLoginService;
import org.springframework.stereotype.Component;

@Component("PasswordAuthLoginServiceFactory")
public class PasswordAuthLoginServiceFactory extends AuthLoginServiceFactory{
    @Override
    public AuthLoginService getAuthService() {
        return new PasswordAuthLoginService();
    }
}
