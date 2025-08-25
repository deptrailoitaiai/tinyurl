package org.example.config.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public MACSigner jwtSigner() throws Exception {
        return new MACSigner(secret.getBytes());
    }

    @Bean
    public MACVerifier jwtVerifier() throws Exception {
        return new MACVerifier(secret.getBytes());
    }

    @Bean
    public JWSAlgorithm jwtAlgorithm() {
        return JWSAlgorithm.HS256;
    }
}