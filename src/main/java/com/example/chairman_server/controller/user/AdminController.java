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

    // 기관 유효성 검증 메서드
    private Institution validateInstitution(Long institutionCode) {
        Institution institution = adminService.findInstitutionByCode(institutionCode);
        if (institution == null) {
            throw new IllegalArgumentException("해당 기관을 찾을 수 없습니다.");
        }
        return institution;
    }

    //관리자 로그인
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestParam Long code) {
        try {
            Institution institution = adminService.loginByCode(code);
            String token = jwtUtil.generateToken(String.valueOf(institution.getInstitutionCode()));
            InstitutionLoginResponse response = new InstitutionLoginResponse(token, institution);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    // 대여 승인
    @PostMapping("/{institutionCode}/reservation/approve/{rentalId}")
    public ResponseEntity<String> approveRental(@PathVariable Long institutionCode, @PathVariable Long rentalId) {
        try {
            Institution institution = validateInstitution(institutionCode);
            adminService.approveRental(rentalId, institution.getInstitutionCode());
            return ResponseEntity.ok("Rental approved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 대여 거절
    @PostMapping("/{institutionCode}/reservation/reject/{rentalId}")
    public ResponseEntity<String> rejectRental(@PathVariable Long institutionCode, @PathVariable Long rentalId) {
        try {
            Institution institution = validateInstitution(institutionCode);
            adminService.rejectRental(rentalId, institution.getInstitutionCode());
            return ResponseEntity.ok("Rental rejected successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 특정 기관의 휠체어 상태별 개수 조회
    @GetMapping("/{institutionCode}/wheelchair/count")
    public ResponseEntity<Map<String, Integer>> getWheelchairCountsByInstitution(@PathVariable Long institutionCode) {
        try {
            Institution institution = validateInstitution(institutionCode);
            Map<String, Integer> counts = wheelchairService.getWheelchairCountsByInstitution(institution);
            return ResponseEntity.ok(counts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 특정 상태의 휠체어 목록 조회
    @GetMapping("/{institutionCode}/wheelchair/list")
    public ResponseEntity<?> getWheelchairsByInstitution(
            @PathVariable Long institutionCode,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        try {
            Institution institution = validateInstitution(institutionCode);

            List<Wheelchair> wheelchairs;
            if ("ALL".equalsIgnoreCase(status)) {
                wheelchairs = wheelchairRepository.findAllByInstitutionInstitutionCode(institutionCode);
            } else {
                WheelchairStatus wheelchairStatus = WheelchairStatus.valueOf(status.toUpperCase());
                wheelchairs = wheelchairRepository.findByInstitutionAndTypeAndStatus(
                        institution, null, wheelchairStatus);
            }
            return ResponseEntity.ok(wheelchairs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("잘못된 상태 값입니다. 사용 가능한 값: AVAILABLE, RENTED, BROKEN, WAITING");
        }
    }

    // 전체 휠체어 통계 조회
    @GetMapping("/wheelchair/count")
    public ResponseEntity<Map<String, Integer>> getGlobalWheelchairCounts() {
        try {
            Map<String, Integer> counts = wheelchairService.getGlobalWheelchairCounts();
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
