package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.dto.rental.RentalRequest;
import com.example.chairman_server.dto.rental.RentalResponse;
import com.example.chairman_server.dto.user.UserCreateRequest;
import com.example.chairman_server.dto.user.UserUpdateRequest;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.user.UserRepository;
import com.example.chairman_server.service.rental.RentalService;
import com.example.chairman_server.service.user.NormalService;
import com.example.chairman_server.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final RentalService rentalService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

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

    // 사용자 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getRentalInfo(@RequestHeader("Authorization") String authorizationHeader) {
        // JWT 토큰에서 이메일 추출
        String token = authorizationHeader.substring(7);  // "Bearer "를 제외한 토큰 부분
        String email = jwtUtil.extractEmail(token);

        // 사용자 정보 가져오기
        Map<String, String> userInfo = userService.getUserInfo(email);

        if (userInfo.containsKey("error")) {
            return ResponseEntity.status(404).body(userInfo.get("error"));
        } else {
            return ResponseEntity.ok(userInfo);
        }
    }

    // 사용자 정보 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String authorizationHeader, @RequestBody UserUpdateRequest updateRequest) {
        String token = authorizationHeader.substring(7);  // "Bearer " 제거
        String email = jwtUtil.extractEmail(token);  // 이메일 추출

        // 사용자 정보 수정
        userService.updateUserInfo(email, updateRequest.getName(), updateRequest.getPhoneNumber(), updateRequest.getAddress());

        return ResponseEntity.ok("사용자 정보가 성공적으로 수정되었습니다.");
    }

}