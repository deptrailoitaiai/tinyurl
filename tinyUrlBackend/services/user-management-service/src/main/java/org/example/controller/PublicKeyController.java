package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PublicKeyController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Endpoint for API Gateway to get JWT verification key
     * Note: In production, consider using RSA public/private key pairs
     */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of(
            "algorithm", "HS256",
            "secret", jwtSecret, // In production, use RSA public key instead
            "issuer", "tinyurl-app"
        ));
    }
}