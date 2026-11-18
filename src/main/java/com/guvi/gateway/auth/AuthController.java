package com.guvi.gateway.auth;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.guvi.gateway.config.AppProperties;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AppProperties properties;
    private final JwtService jwtService;

    public AuthController(AppProperties properties, JwtService jwtService) {
        this.properties = properties;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        if (!properties.demo().username().equals(request.username())
                || !properties.demo().password().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return new TokenResponse(jwtService.generateToken(request.username()), "Bearer", properties.jwt().expirationMinutes() * 60);
    }

    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
}
