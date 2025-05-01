package com.gtu.auth_service.infraestructure.service;


import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gtu.auth_service.infraestructure.client.dto.UserServiceResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${JWT_SECRET}")
    private String secretKey;
    private long jwtExpiration;
    public JwtService(){
        jwtExpiration = 30L * 60000;
    }

    public String generateToken(UserServiceResponse userDetails) {
        return buildToken(userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            
            UserServiceResponse userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claim("user-id", userDetails.getId())
                .claim("user-email", userDetails.getEmail())
                .claim("user-name", userDetails.getName())
                .signWith(getSignInKey())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .compact();
    }


    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}