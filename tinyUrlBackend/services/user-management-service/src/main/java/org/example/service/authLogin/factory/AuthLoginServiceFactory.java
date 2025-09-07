package org.example.service.authLogin.factory;

import org.example.service.authLogin.AuthLoginService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthLoginServiceFactory {
    public abstract AuthLoginService getAuthService();
}
