package com.aegis.saas.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT operations using the JJWT 0.12.x API (all deprecated 0.11.x methods removed).
 */
@Service
public class JWTService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSecretKey() {
        byte[] encoded = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(encoded);
    }

    public String generateToken(String email, Long tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String email) {
        Date now = new Date(System.currentTimeMillis());
        Date expiry = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String email) {
        return extractSubject(token).equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }
}
