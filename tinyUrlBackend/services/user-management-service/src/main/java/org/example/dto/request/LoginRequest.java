package org.example.dto.request;

import lombok.Data;
import org.example.service.authLogin.factory.AuthLoginServiceFactoryFinder;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private AuthLoginServiceFactoryFinder.AuthenticationType loginType; // PASSWORD, GOOGLE, FACEBOOK
    private String googleToken;
    private String facebookToken;
}
