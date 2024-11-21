package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.dto.rental.RentalRequest;
import com.example.chairman_server.service.rental.RentalService;
import com.example.chairman_server.service.user.NormalService;
import com.example.chairman_server.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final RentalService rentalService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 일반 사용자 페이지 환영 메시지
    @GetMapping
    public ResponseEntity<String> userPage() {
        return ResponseEntity.ok("Welcome to the user page");
    }

    // 특정 공공기관에서의 대여 가능한 휠체어 개수 조회
    @GetMapping("/{institutionCode}/available-count")
    public ResponseEntity<Map<String, Integer>> getAvailableWheelchairCounts(@PathVariable Long institutionCode) {
        Map<String, Integer> availableCounts = rentalService.getAvailableWheelchairCounts(institutionCode);
        return ResponseEntity.ok(availableCounts);
    }

    // 모든 공공기관 목록 조회
    @GetMapping("/institutions")
    public ResponseEntity<List<InstitutionData>> getAllInstitutions() {
        List<InstitutionData> institutions = userService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }



}