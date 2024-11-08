package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.dto.user.LoginRequest;
import com.example.chairman_server.dto.user.UserCreateRequest;
import com.example.chairman_server.service.user.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 회원가입 처리
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserCreateRequest userCreateRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation errors occurred");
        }
        try {
            userService.create(userCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Signup failed");
        }
    }

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("Login page placeholder");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            // 로그인 인증 처리
            Authentication authentication = userService.authenticate(email, password);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(userDetails.getUsername());

            // 디버깅용 로그
            System.out.println("Login successful, returning token: " + token);

            // 응답에 토큰 포함
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (Exception e) {
            // 예외 발생 시 UNAUTHORIZED 반환
            System.out.println("Login failed due to: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // 로그아웃 처리 (JWT는 상태 저장 없이, 클라이언트에서 삭제 처리)
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token provided, logout failed");
    }

    @GetMapping("/current")
    public User getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email = jwtUtil.extractEmail(jwt); // JWT에서 이메일 추출
        return userService.getCurrentUser(email);
    }


    @GetMapping("/authStatus")
    public ResponseEntity<?> getAuthStatus(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            try {
                // JWT가 유효한지 확인
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractEmail(token);
                    return ResponseEntity.ok("User authenticated: " + username);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
                }
            } catch (MalformedJwtException e) {
                // 잘못된 JWT 형식에 대한 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Malformed JWT Token");
            } catch (ExpiredJwtException e) {
                // 만료된 JWT에 대한 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired JWT Token");
            } catch (JwtException e) {
                // 기타 JWT 관련 에러에 대한 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT Token error");
            }
        }

        // Authorization 헤더가 없는 경우
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header missing");
    }

}