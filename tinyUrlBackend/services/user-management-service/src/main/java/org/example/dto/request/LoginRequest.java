package org.example.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String loginType; // PASSWORD, GOOGLE, FACEBOOK
    private String googleToken;
    private String facebookToken;
}
