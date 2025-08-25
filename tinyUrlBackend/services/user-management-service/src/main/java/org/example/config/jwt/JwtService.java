package org.example.config.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final MACSigner signer;
    private final MACVerifier verifier;
    private final JWSAlgorithm algorithm;

    @Value("${jwt.expiration}")
    private long expiration;

    public JwtService(MACSigner signer, MACVerifier verifier, JWSAlgorithm algorithm) {
        this.signer = signer;
        this.verifier = verifier;
        this.algorithm = algorithm;
    }

    // Generate token
    public String generateAccessToken(Long userId) throws JOSEException {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .issuer("tinyurl-app")
                .issueTime(now)
                .expirationTime(exp)
                .build();

        JWSHeader header = new JWSHeader(algorithm);
        SignedJWT signedJWT = new SignedJWT(header, claims);

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String generateRefreshToken(Long userId) throws JOSEException {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .issuer("tinyurl-app")
                .issueTime(now)
                .expirationTime(exp)
                .build();

        JWSHeader header = new JWSHeader(algorithm);
        SignedJWT signedJWT = new SignedJWT(header, claims);

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            return exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return Long.parseLong(signedJWT.getJWTClaimsSet().getSubject());
    }
}