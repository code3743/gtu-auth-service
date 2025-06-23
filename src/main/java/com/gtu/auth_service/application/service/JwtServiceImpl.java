package com.gtu.auth_service.application.service;


import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.service.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${JWT_SECRET}")
    private String secretKey;
    private long jwtExpiration;
    public JwtServiceImpl(){
        jwtExpiration = 30L * 60000;
    }

    public String generateToken(AuthUser userDetails) {
        return buildToken(userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            
            AuthUser userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claim("user-id", userDetails.id())
                .claim("user-email", userDetails.email())
                .claim("user-name", userDetails.name())
                .claim("user-role", userDetails.role().name())
                .signWith(getSignInKey())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .compact();
    }


    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}