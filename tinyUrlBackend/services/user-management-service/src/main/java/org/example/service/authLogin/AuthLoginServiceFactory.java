package org.example.service.authLogin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AuthLoginServiceFactory {

    private final AuthLoginService googleAuthLoginService;
    private final AuthLoginService passwordAuthLoginService;
    private final AuthLoginService facebookAuthLoginService;

    @Autowired
    public AuthLoginServiceFactory(
            @Qualifier("googleAuthLoginService") AuthLoginService googleAuthLoginService,
            @Qualifier("passwordAuthLoginService") AuthLoginService passwordAuthLoginService,
            @Qualifier("facebookAuthLoginService") AuthLoginService facebookAuthLoginService) {
        this.googleAuthLoginService = googleAuthLoginService;
        this.passwordAuthLoginService = passwordAuthLoginService;
        this.facebookAuthLoginService = facebookAuthLoginService;
    }

    public AuthLoginService getAuthLoginService(String loginType) {
        switch (loginType.toUpperCase()) {
            case "GOOGLE":
                return googleAuthLoginService;
            case "PASSWORD":
                return passwordAuthLoginService;
            case "FACEBOOK":
                return facebookAuthLoginService;
            default:
                throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }
    }
}
