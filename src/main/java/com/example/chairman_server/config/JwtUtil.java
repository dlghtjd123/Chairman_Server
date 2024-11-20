package com.example.chairman_server.config;

import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.repository.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:36000000}") // 기본값 10시간
    private long jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // 적합한 길이(256비트)의 키 생성
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret key는 최소 32자 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // 적절한 키 생성
    }

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Map<String, Object> getUserInfoFromToken(String token) {
        Claims claims = extractAllClaims(token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", claims.get("userId"));
        userInfo.put("email", claims.getSubject());
        userInfo.put("role", claims.get("role"));
        return userInfo;
    }
}
