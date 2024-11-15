package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.dto.user.InstitutionLoginResponse;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.service.user.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InstitutionRepository institutionRepository;
    private final JwtUtil jwtUtil;

    // 관리자 로그인
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestParam Long code) {
        Institution institution = adminService.loginByCode(code);
        if (institution == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("관리자 코드가 올바르지 않습니다.");
        }
        String token = jwtUtil.generateToken(String.valueOf(institution.getInstitutionCode()));
        InstitutionLoginResponse response = new InstitutionLoginResponse(token, institution);
        return ResponseEntity.ok(response);
    }

    // 관리자 페이지 환영 메시지
    @GetMapping
    public ResponseEntity<String> adminPage() {
        return ResponseEntity.ok("Welcome to the admin page");
    }

    // 대여 승인
    @PostMapping("/{institutionCode}/reservation/approve/{rentalId}")
    public ResponseEntity<String> approveRental(@PathVariable String institutionCode, @PathVariable Long rentalId) {
        adminService.approveRental(rentalId);
        return ResponseEntity.ok("Rental approved successfully.");
    }

    // 대여 거절
    @PostMapping("/{institutionCode}/reservation/reject/{rentalId}")
    public ResponseEntity<String> rejectRental(@PathVariable String institutionCode, @PathVariable Long rentalId) {
        adminService.rejectRental(rentalId);
        return ResponseEntity.ok("Rental rejected successfully.");
    }

    // 휠체어 상태 통계 조회
    @GetMapping("/{institutionCode}/rentals")
    public ResponseEntity<Map<String, Long>> getWheelchairStatusCounts(@PathVariable String institutionCode) {
        Map<String, Long> statusCounts = adminService.getWheelchairStatusCounts();
        return ResponseEntity.ok(statusCounts);
    }
}
