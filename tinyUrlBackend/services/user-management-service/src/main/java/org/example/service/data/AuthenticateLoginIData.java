package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.service.authLogin.factory.AuthLoginServiceFactoryFinder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticateLoginIData {
    private String email;
    private String password;
    private AuthLoginServiceFactoryFinder.AuthenticationType loginType; // PASSWORD, GOOGLE, FACEBOOK
    private String googleToken;
    private String facebookToken;
}
