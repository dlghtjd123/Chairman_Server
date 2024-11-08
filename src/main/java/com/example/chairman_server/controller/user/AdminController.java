package com.example.chairman_server.controller.user;

import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.service.user.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private InstitutionRepository institutionRepository;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestParam String code) {
        // 입력된 코드로 기관을 찾음
        return institutionRepository.findByInstitutionCode(code)
                .map(institution -> ResponseEntity.ok("관리자 로그인 성공"))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("관리자 코드가 올바르지 않습니다."));
    }

    // 관리자 페이지 환영 메시지
    @GetMapping
    public ResponseEntity<String> adminPage() {
        return ResponseEntity.ok("Welcome to the admin page");
    }

    // 대여 승인
    @PostMapping("/reservation/approve/{rentalId}")
    public ResponseEntity<String> approveRental(@PathVariable Long rentalId) {
        adminService.approveRental(rentalId);
        return ResponseEntity.ok("Rental approved successfully.");
    }

    // 대여 거절
    @PostMapping("/reservation/reject/{rentalId}")
    public ResponseEntity<String> rejectRental(@PathVariable Long rentalId) {
        adminService.rejectRental(rentalId);
        return ResponseEntity.ok("Rental rejected successfully.");
    }

    // 휠체어 상태 통계 조회
    @GetMapping("/rentals")
    public ResponseEntity<Map<String, Long>> getWheelchairStatusCounts() {
        Map<String, Long> statusCounts = adminService.getWheelchairStatusCounts();
        return ResponseEntity.ok(statusCounts);
    }
}
