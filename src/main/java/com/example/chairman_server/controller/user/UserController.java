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

    // 대여 폼 보여주기 (특정 공공기관의 휠체어 종류 반환)
    @GetMapping("/{institutionCode}/rent")
    public ResponseEntity<List<WheelchairType>> showRentForm(@PathVariable String institutionCode) {
        // institutionCode를 사용하여 필요한 데이터를 조회할 수 있습니다.
        return ResponseEntity.ok(Arrays.asList(WheelchairType.values()));
    }

    // 특정 공공기관에서의 대여 가능한 휠체어 개수 조회
    @GetMapping("/{institutionCode}/available-count")
    public ResponseEntity<Map<String, Integer>> getAvailableWheelchairCounts(@PathVariable Long institutionCode) {
        Map<String, Integer> availableCounts = rentalService.getAvailableWheelchairCounts(institutionCode);
        return ResponseEntity.ok(availableCounts);
    }

    // 특정 공공기관 및 휠체어 타입에 따른 대여 가능한 날짜 조회
    @GetMapping("/{institutionCode}/{wheelchairType}/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @PathVariable Long institutionCode,
            @PathVariable String wheelchairType
    ) {
        List<LocalDate> availableDates = rentalService.findAvailableDates(institutionCode, WheelchairType.valueOf(wheelchairType.toUpperCase()));
        return ResponseEntity.ok(availableDates);
    }

    // 특정 공공기관에서의 휠체어 대여 처리
    @PostMapping("/{institutionCode}/rent")
    public ResponseEntity<Rental> rentWheelchair(
            @PathVariable Long institutionCode, // 공공기관 코드
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody RentalRequest rentRequest
    ) {
        System.out.println("Institution Code: " + institutionCode);
        System.out.println("Authorization Header: " + authorizationHeader);
        System.out.println("Wheelchair Type: " + rentRequest.getWheelchairType());
        System.out.println("Rental Date: " + rentRequest.getRentalDate());
        System.out.println("Return Date: " + rentRequest.getReturnDate());

        String token = authorizationHeader.substring(7); // "Bearer " 제거
        String username = jwtUtil.extractEmail(token); // JWT에서 사용자 이름 추출
        LocalDateTime returnDateTime = LocalDateTime.parse(rentRequest.getReturnDate());
        LocalDateTime rentalDateTime = LocalDateTime.parse(rentRequest.getRentalDate());

        Rental rental = rentalService.rentWheelchair(institutionCode, username, rentRequest.getWheelchairType(), rentalDateTime, returnDateTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    // 모든 공공기관 목록 조회
    @GetMapping("/institutions")
    public ResponseEntity<List<InstitutionData>> getAllInstitutions() {
        List<InstitutionData> institutions = userService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }

    // 특정 공공기관의 대시보드 조회
    @GetMapping("/institutions/{institutionCode}/dashboard")
    public ResponseEntity<InstitutionData> getInstitutionDashboard(@PathVariable Long institutionCode) {
        InstitutionData institutionData = userService.getInstitutionData(institutionCode);
        return ResponseEntity.ok(institutionData);
    }
}