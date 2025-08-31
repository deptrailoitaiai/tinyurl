package org.example.service.authLogin.factory;

import org.example.service.authLogin.AuthLoginService;
import org.example.service.authLogin.FacebookAuthLoginService;
import org.springframework.stereotype.Component;

@Component("FacebookAuthLoginServiceFactory")
public class FacebookAuthLoginServiceFactory extends AuthLoginServiceFactory{
    @Override
    public AuthLoginService getAuthService() {
        return new FacebookAuthLoginService();
    }
}
