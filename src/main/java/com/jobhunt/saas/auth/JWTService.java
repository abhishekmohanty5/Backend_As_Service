package com.jobhunt.saas.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public Key getSecretKey()
    {
        byte[] encoded = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(encoded);
    }

    public String generateToken(String email, Long tenantId)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        return createToken(claims, email);
    }

    private String  createToken(Map<String,Object> claims,String email){
        Date now = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(now.getTime() + jwtExpiration);

         return Jwts.builder()
                 .setClaims(claims)
                 .setSubject(email)
                 .setIssuedAt(now)
                 .setExpiration(expirationDate)
                 .signWith(getSecretKey(),SignatureAlgorithm.HS256)
                 .compact();
    }


    //This Method Verify My Token and Return Subject right
    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token).getBody();
    }
    public boolean isTokenValid(String token, String email) {
        final String subject = extractSubject(token);
        return (subject.equals(email) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }


}
