package com.example.chairman_server.controller.Institution;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.service.Institution.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;
    private final JwtUtil jwtUtil;  // JWT 생성 유틸리티

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String institutionCode) {
        Institution institution = institutionService.loginByCode(institutionCode);
        String token = jwtUtil.generateToken(institution.getInstitutionCode()); // JWT 생성
        return ResponseEntity.ok(Map.of("token", token, "institution", institution));
    }
}

