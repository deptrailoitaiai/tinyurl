package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticateLoginIData {
    private String email;
    private String password;
    private String loginType; // PASSWORD, GOOGLE, FACEBOOK
    private String googleToken;
    private String facebookToken;
}
