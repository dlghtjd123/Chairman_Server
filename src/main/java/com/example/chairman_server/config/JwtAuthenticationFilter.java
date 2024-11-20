package com.example.chairman_server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            String authorizationHeader = request.getHeader("Authorization");

            // 헤더 유효성 검증
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7); // "Bearer " 제거

                // JWT에서 사용자 이메일 추출
                String email = jwtUtil.extractEmail(jwtToken);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 사용자 정보 로드
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // JWT 유효성 검증
                    if (jwtUtil.validateToken(jwtToken)) {
                        // 인증 객체 생성 및 SecurityContext에 설정
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            }
        } catch (Exception e) {
            // JWT 처리 중 예외 발생 시 로그 출력
            System.err.println("JWT 인증 오류: " + e.getMessage());
        }

        // 다음 필터 체인 실행
        chain.doFilter(request, response);
    }
}
