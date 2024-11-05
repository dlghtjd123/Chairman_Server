package com.example.chairman_server.controller.user;

import com.example.chairman_server.service.user.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

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
