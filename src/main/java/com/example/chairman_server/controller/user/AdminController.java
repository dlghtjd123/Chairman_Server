package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.dto.user.InstitutionLoginResponse;
import com.example.chairman_server.service.user.AdminService;
import com.example.chairman_server.service.wheelchair.WheelchairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final WheelchairService wheelchairService;
    private final WheelchairRepository wheelchairRepository;
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

    // 특정 Institution의 휠체어 상태별 개수 조회
    @GetMapping("/{institutionCode}/wheelchair/count")
    public ResponseEntity<Map<String, Integer>> getWheelchairCountsByInstitution(@PathVariable Long institutionCode) {
        Institution institution = adminService.findInstitutionByCode(institutionCode); // institutionCode로 Institution 찾기
        if (institution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        int available = wheelchairRepository.countByInstitutionAndTypeAndStatus(institution, null, WheelchairStatus.AVAILABLE);
        int broken = wheelchairRepository.countByInstitutionAndTypeAndStatus(institution, null, WheelchairStatus.BROKEN);
        int rented = wheelchairRepository.countByInstitutionAndTypeAndStatus(institution, null, WheelchairStatus.RENTED);
        int waiting = wheelchairRepository.countByInstitutionAndTypeAndStatus(institution, null, WheelchairStatus.WAITING);

        Map<String, Integer> counts = new HashMap<>();
        counts.put("available", available);
        counts.put("broken", broken);
        counts.put("rented", rented);
        counts.put("waiting", waiting);

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/{institutionCode}/wheelchair/list/{status}")
    public ResponseEntity<List<Wheelchair>> getWheelchairsByInstitutionAndStatus(
            @PathVariable Long institutionCode, @PathVariable WheelchairStatus status) {
        Institution institution = adminService.findInstitutionByCode(institutionCode);
        List<Wheelchair> wheelchairs = wheelchairRepository.findByInstitutionAndTypeAndStatus(institution, null, status);
        return ResponseEntity.ok(wheelchairs);
    }


    // 전체 휠체어 목록 조회 (관리자)
    @GetMapping("/{institutionCode}/wheelchair")
    public ResponseEntity<?> getWheelchairsByInstitution(@PathVariable Long institutionCode,
                                                         @RequestParam(required = false) String status) {
        // Institution 조회
        Institution institution = adminService.findInstitutionByCode(institutionCode);
        if (institution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 기관을 찾을 수 없습니다.");
        }

        List<Wheelchair> wheelchairs;

        try {
            // Status가 주어지지 않은 경우, 전체 휠체어 조회
            if (status == null || "ALL".equalsIgnoreCase(status)) {
                wheelchairs = wheelchairRepository.findAllByInstitutionInstitutionCode(institutionCode);
            } else {
                // 특정 상태의 휠체어 조회
                WheelchairStatus wheelchairStatus = WheelchairStatus.valueOf(status.toUpperCase());
                wheelchairs = wheelchairRepository.findByInstitutionAndTypeAndStatus(
                        institution, null, wheelchairStatus
                );
            }
            return ResponseEntity.ok(wheelchairs);
        } catch (IllegalArgumentException e) {
            // 잘못된 Status 값 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("잘못된 상태 값입니다. 사용 가능한 값: AVAILABLE, RENTED, BROKEN, WAITING");
        }
    }


    // 관리자 대시보드: 전체 휠체어 통계 조회
    @GetMapping("/wheelchair/count")
    public ResponseEntity<Map<String, Integer>> getGlobalWheelchairCounts() {
        int total = wheelchairService.countAll();
        int available = wheelchairService.countByStatus(WheelchairStatus.AVAILABLE);
        int broken = wheelchairService.countByStatus(WheelchairStatus.BROKEN);
        int rented = wheelchairService.countByStatus(WheelchairStatus.RENTED);
        int waiting = wheelchairService.countByStatus(WheelchairStatus.WAITING);

        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", total);
        counts.put("available", available);
        counts.put("broken", broken);
        counts.put("rented", rented);
        counts.put("waiting", waiting);

        return ResponseEntity.ok(counts);
    }
}
