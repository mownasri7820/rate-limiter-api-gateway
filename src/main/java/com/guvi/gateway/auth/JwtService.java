package com.guvi.gateway.auth;

import com.guvi.gateway.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final AppProperties properties;
    private final SecretKey key;

    public JwtService(AppProperties properties) {
        this.properties = properties;
        byte[] bytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder().subject(username).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.jwt().expirationMinutes() * 60)))
                .signWith(key).compact();
    }

    public String validateAndGetSubject(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
                .getPayload().getSubject();
    }
}
