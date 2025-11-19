package com.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.demo.entity.User;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    // 512-bit secret key for HS512 (must be at least 64 characters)
    private static final String SECRET = "MyVeryLongSecretKeyForJWTGenerationThatIsAtLeast64CharactersLongForHS512Algorithm";
    private static final long EXPIRATION = 24*60*60*1000; // 1 day in milliseconds

    private final SecretKey secretKey;

    public JwtUtil() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
        claims.put("userId", user.getId());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            return validateToken(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}