package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.wheelchair.WheelchairDetailResponse;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.dto.user.InstitutionLoginResponse;
import com.example.chairman_server.service.institution.InstitutionService;
import com.example.chairman_server.service.rental.RentalService;
import com.example.chairman_server.service.user.AdminService;
import com.example.chairman_server.service.wheelchair.WheelchairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final WheelchairService wheelchairService;
    private final WheelchairRepository wheelchairRepository;
    private final InstitutionService institutionService;
    private final JwtUtil jwtUtil;
    private final RentalRepository rentalRepository;

    // 기관 유효성 검증 메서드
    private Institution validateInstitution(Long institutionCode) {
        Institution institution = adminService.findInstitutionByCode(institutionCode);
        if (institution == null) {
            throw new IllegalArgumentException("해당 기관을 찾을 수 없습니다.");
        }
        return institution;
    }

    // 관리자 로그인
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
            // 기관 검증
            Institution institution = validateInstitution(institutionCode);

            // Rental 객체 가져오기
            Rental rental = rentalRepository.findById(rentalId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대여 요청을 찾을 수 없습니다."));

            // Rental 상태를 REJECTED로 설정
            rental.setStatus(RentalStatus.NORMAL);

            // user_id를 null로 설정
            rental.setUser(null);

            // 변경사항 저장
            rentalRepository.save(rental);

            // 서비스 호출
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
            @RequestParam(required = false) String status) {

        // 기관 유효성 확인
        Institution institution = institutionService.findByInstitutionCode(institutionCode);
        if (institution == null) {
            throw new RuntimeException("기관을 찾을 수 없습니다.");
        }

        // 휠체어 목록 조회
        List<Wheelchair> wheelchairs;
        if ("ALL".equalsIgnoreCase(status)) {
            wheelchairs = wheelchairRepository.findByInstitution(institution);
        } else {
            WheelchairStatus wheelchairStatus = WheelchairStatus.valueOf(status.toUpperCase());
            wheelchairs = wheelchairRepository.findByInstitutionAndStatus(institution, wheelchairStatus);
        }

        // 휠체어 세부 정보 생성
        List<WheelchairDetailResponse> response = wheelchairs.stream()
                .map(wheelchair -> {
                    // 모든 상태를 고려하여 Rental 조회
                    List<Rental> rentals = rentalRepository.findAllByWheelchair(wheelchair);

                    // 기본값 설정
                    String rentalStatus = "NORMAL";
                    String userName = "정보 없음";
                    String userPhone = "정보 없음";

                    // 가장 최신의 Rental 데이터로 상태 및 사용자 정보 설정
                    if (!rentals.isEmpty()) {
                        Rental latestRental = rentals.get(0); // 가장 최근 Rental 가져오기
                        rentalStatus = latestRental.getStatus().name();

                        if (latestRental.getUser() != null) {
                            userName = latestRental.getUser().getName();
                            userPhone = latestRental.getUser().getPhoneNumber();
                        }
                    }

                    // WheelchairDetailResponse 반환
                    return new WheelchairDetailResponse(
                            wheelchair.getWheelchairId(),
                            wheelchair.getType().name(),
                            wheelchair.getStatus().name(),
                            rentalStatus,
                            userName,
                            userPhone
                    );
                })
                .collect(Collectors.toList());

        // Response 반환
        return ResponseEntity.ok(response);
    }




    @PutMapping("/wheelchair/{wheelchairId}/status")
    public ResponseEntity<?> updateWheelchairAndRentalStatus(
            @PathVariable Long wheelchairId,
            @RequestParam String wheelchairStatus,
            @RequestParam String rentalStatus) {

        Wheelchair wheelchair = wheelchairRepository.findById(wheelchairId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 휠체어를 찾을 수 없습니다."));

        // Wheelchair 상태 변경
        WheelchairStatus newWheelchairStatus = WheelchairStatus.valueOf(wheelchairStatus.toUpperCase());
        wheelchair.setStatus(newWheelchairStatus);



        // 특정 상태에서 user_id 초기화
        if (newWheelchairStatus == WheelchairStatus.AVAILABLE) {
            wheelchair.removeUser(); // user_id를 null로 설정
        }

        wheelchairRepository.save(wheelchair);

        // Rental 삭제 로직 추가
        if (newWheelchairStatus == WheelchairStatus.AVAILABLE) {
            Optional<Rental> rentalOptional = rentalRepository.findByWheelchair(wheelchair);
            rentalOptional.ifPresent(rental -> rentalRepository.delete(rental)); // Rental 삭제
        }

        // Rental 상태 변경
        Optional<Rental> rentalOptional = rentalRepository.findByWheelchair(wheelchair);
        if (rentalOptional.isPresent()) {
            Rental rental = rentalOptional.get();
            RentalStatus newRentalStatus = RentalStatus.valueOf(rentalStatus.toUpperCase());
            rental.setStatus(newRentalStatus);

            // Rental 상태에 따른 user 초기화
            if (newRentalStatus == RentalStatus.NORMAL) {
                rental.setUser(null); // user_id를 null로 설정
            }
            rentalRepository.save(rental);
        }

        return ResponseEntity.ok("휠체어 및 대여 상태가 성공적으로 업데이트되었습니다.");
    }



    @PutMapping("/wheelchair/{wheelchairId}/onlyStatus")
    public ResponseEntity<?> updateWheelchairStatus(
            @PathVariable Long wheelchairId,
            @RequestParam String status) {

        Wheelchair wheelchair = wheelchairRepository.findById(wheelchairId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 휠체어를 찾을 수 없습니다."));

        try {
            // 휠체어 상태 업데이트
            WheelchairStatus newStatus = WheelchairStatus.valueOf(status.toUpperCase());
            wheelchair.setStatus(newStatus);

            // AVAILABLE 상태로 변경 시 사용자 초기화
            if (newStatus == WheelchairStatus.AVAILABLE) {
                wheelchair.removeUser(); // user_id를 null로 설정
            }

            // AVAILABLE 상태로 변경 시 Rental 삭제
            if (newStatus == WheelchairStatus.AVAILABLE) {
                Optional<Rental> rentalOptional = rentalRepository.findByWheelchair(wheelchair);
                rentalOptional.ifPresent(rental -> rentalRepository.delete(rental)); // Rental 삭제
            }

            wheelchairRepository.save(wheelchair);
            return ResponseEntity.ok("휠체어 상태가 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 상태 값: " + e.getMessage());
        }
    }

}
