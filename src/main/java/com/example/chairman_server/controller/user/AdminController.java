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
import com.example.chairman_server.repository.user.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;

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
    @Transactional // 트랜잭션 활성화
    public ResponseEntity<?> updateWheelchairAndRentalStatus(
            @PathVariable Long wheelchairId,
            @RequestParam String wheelchairStatus,
            @RequestParam String rentalStatus) {

        // 휠체어 엔티티 조회
        Wheelchair wheelchair = wheelchairRepository.findById(wheelchairId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 휠체어를 찾을 수 없습니다."));

        // 휠체어 상태 업데이트
        WheelchairStatus newWheelchairStatus = WheelchairStatus.valueOf(wheelchairStatus.toUpperCase());
        wheelchair.setStatus(newWheelchairStatus);
        wheelchairRepository.save(wheelchair); // 휠체어 상태 저장

        // Rental 조회
        Optional<Rental> rentalOptional = rentalRepository.findByWheelchair(wheelchair);

        // Rental이 존재하면 상태 업데이트 또는 삭제 처리
        if (rentalOptional.isPresent()) {
            Rental rental = rentalOptional.get();

            if (newWheelchairStatus == WheelchairStatus.AVAILABLE) {
                wheelchair.removeUser();

                User user = rental.getUser();
                if (user != null) {
                    user.setStatus(RentalStatus.NORMAL); // User 상태를 NORMAL로 설정
                    userRepository.save(user);
                }
                rentalRepository.delete(rental); // Rental 삭제
            } else {
                // Rental 상태 업데이트
                RentalStatus newRentalStatus = RentalStatus.valueOf(rentalStatus.toUpperCase());
                rental.setStatus(newRentalStatus);

                // User 상태 업데이트
                User user = rental.getUser();
                if (user != null) {
                    if (newRentalStatus == RentalStatus.ACTIVE) {
                        user.setStatus(RentalStatus.ACTIVE);
                    } else if (newRentalStatus == RentalStatus.NORMAL) {
                        user.setStatus(RentalStatus.NORMAL);
                        rental.setUser(null); // Rental에서 User 해제
                    }
                    userRepository.save(user); // User 상태 저장
                }
                rentalRepository.save(rental); // Rental 상태 저장
            }
        }

        return ResponseEntity.ok("휠체어 상태 및 대여 상태가 성공적으로 업데이트되었습니다.");
    }



    // 휠체어 상태만 업데이트
    @PutMapping("/wheelchair/{wheelchairId}/onlyStatus")
    public ResponseEntity<?> updateWheelchairStatus(
            @PathVariable Long wheelchairId,
            @RequestParam String status) {

        Wheelchair wheelchair = wheelchairRepository.findById(wheelchairId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 휠체어를 찾을 수 없습니다."));

        WheelchairStatus newStatus = WheelchairStatus.valueOf(status.toUpperCase());
        wheelchair.setStatus(newStatus);

        // 상태가 AVAILABLE이면 Rental 삭제 및 User 상태 NORMAL로 변경
        if (newStatus == WheelchairStatus.AVAILABLE) {
            Optional<Rental> rentalOptional = rentalRepository.findByWheelchair(wheelchair);
            rentalOptional.ifPresent(rental -> {
                User user = rental.getUser();
                if (user != null) {
                    user.setStatus(RentalStatus.NORMAL);
                    userRepository.save(user);
                }
                rentalRepository.delete(rental);
            });
        }

        wheelchairRepository.save(wheelchair);
        return ResponseEntity.ok("휠체어 상태가 성공적으로 업데이트되었습니다.");
    }

}
