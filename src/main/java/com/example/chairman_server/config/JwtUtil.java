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

    private SecretKey key;

    private final UserRepository userRepository;

    // SecretKey를 한 번만 생성하는 방식으로 수정
    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);  // 안전한 256비트 키를 한 번 생성
    }

    // 토큰 생성 메서드
    public String generateToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("phoneNumber", user.getPhoneNumber());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10시간 유효
                .signWith(key, SignatureAlgorithm.HS256)  // 동일한 key를 사용해 서명
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class); // userId를 반환
    }

    // 토큰 검증 메서드
    public boolean validateToken(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);  // 토큰 검증 시 에러가 발생하지 않으면 유효
            return true;
        } catch (Exception e) {
            return false;  // 토큰이 유효하지 않으면 false 반환
        }
    }

    // 이메일 추출
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 토큰 만료 확인
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // 모든 클레임 추출 (토큰 검증 시 동일한 SecretKey 사용)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public User getUserFromToken(String token) {
        // Claims 객체에서 JWT 정보를 파싱
        Claims claims = extractAllClaims(token);

        // Claims에서 필요한 정보 추출
        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();

        // 사용자 객체 생성 후 반환
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        // 필요한 다른 필드도 설정

        return user;
    }
}
