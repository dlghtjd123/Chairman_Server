package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.wheelchair.WheelchairDetailResponse;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.dto.user.InstitutionLoginResponse;
import com.example.chairman_server.service.user.AdminService;
import com.example.chairman_server.service.wheelchair.WheelchairService;
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

    @GetMapping("/{institutionCode}/status-count")
    public ResponseEntity<Map<String, Long>> getWheelchairStatusCount(@PathVariable Long institutionCode) {
        Map<String, Long> statusCounts = wheelchairService.getStatusCounts(institutionCode);
        return ResponseEntity.ok(statusCounts);
    }


    @GetMapping("/{institutionCode}/details")
    public ResponseEntity<List<WheelchairDetailResponse>> getWheelchairDetails(
            @PathVariable Long institutionCode,
            @RequestParam("status") String status) {
        try {
            // "ALL" 상태는 Service에서 처리
            List<WheelchairDetailResponse> details = wheelchairService.getDetailsByStatus(institutionCode, status);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 상태가 잘못된 경우
        }
    }


}
