package com.example.chairman_server.controller.rental;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.rental.RentalRequest;
import com.example.chairman_server.dto.rental.RentalResponse;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.user.UserRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.service.rental.RentalService;
import com.example.chairman_server.service.wheelchair.WheelchairService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/rental")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final WheelchairService wheelchairService;
    private final JwtUtil jwtUtil; // JwtUtil 주입
    private final WheelchairRepository wheelchairRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    @PostMapping("/{institutionCode}/rent")
    public ResponseEntity<?> rentWheelchair(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long institutionCode,
            @RequestBody RentalRequest rentRequest
    ) {
        try {
            // JWT 토큰에서 사용자 이메일 추출
            String token = authorizationHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            // 날짜/시간 파싱
            LocalDateTime rentalDateTime = LocalDateTime.parse(rentRequest.getRentalDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            LocalDateTime returnDateTime = LocalDateTime.parse(rentRequest.getReturnDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            // 날짜 검증
            if (returnDateTime.isBefore(rentalDateTime)) {
                return ResponseEntity.badRequest().body("반납일은 대여일과 같거나 이후여야 합니다.");
            }

            // 대여 처리
            Rental rental = rentalService.rentWheelchair(
                    institutionCode,
                    email,
                    rentRequest.getWheelchairType(),
                    rentalDateTime,
                    returnDateTime
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(rental);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("잘못된 날짜 형식입니다. yyyy-MM-dd'T'HH:mm:ss 형식을 사용하세요.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("대여 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    // 대여 요청 승인
    @PutMapping("/{institutionCode}/approve/{rentalId}")
    public ResponseEntity<Rental> approveRental(
            @PathVariable Long institutionCode,
            @PathVariable Long rentalId) {

        Rental rental = rentalService.approveRental(rentalId);
        return ResponseEntity.ok(rental);
    }

    // 대여 요청 거절
    @PutMapping("/{institutionCode}/reject/{rentalId}")
    public ResponseEntity<Rental> rejectRental(
            @PathVariable Long institutionCode,
            @PathVariable Long rentalId) {

        Rental rental = rentalService.rejectRental(rentalId);
        return ResponseEntity.ok(rental);
    }

    @PostMapping("/{institutionCode}/cancel")
    public ResponseEntity<?> cancelWheelchair(
            @PathVariable Long institutionCode,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7); // "Bearer " 제거
        String email = rentalService.extractEmailFromToken(token);

        // 대여 취소 처리
        rentalService.cancelWheelchair(email);

        return ResponseEntity.ok("대여가 성공적으로 취소되었습니다.");
    }

    // 대기 중인 대여 요청 목록 조회
    @GetMapping("/{institutionCode}/list")
    public ResponseEntity<List<Rental>> getWaitingRentals(@PathVariable Long institutionCode) {
        try {
            // institutionCode 확인을 위한 로그
            log.info("받은 institutionCode: {}", institutionCode);

            List<Rental> rentals = rentalService.getRentalsByStatusAndInstitution(
                    RentalStatus.WAITING, institutionCode);
            return ResponseEntity.ok(rentals);
        } catch (Exception e) {
            log.error("대여 대기 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //현재 사용자의 대여 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getRentalInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

        Optional<Rental> rentalOptional = rentalRepository.findCurrentRentalByUser(user);

        if (rentalOptional.isPresent()) {
            Rental rental = rentalOptional.get();
            Wheelchair wheelchair = rental.getWheelchair();
            Institution institution = wheelchair.getInstitution();

            RentalResponse response = new RentalResponse();
            response.setRentalId(rental.getRentalId());
            response.setRentalDate(rental.getRentalDate().toString());
            response.setReturnDate(rental.getReturnDate() != null ? rental.getReturnDate().toString() : "반납 예정 없음");
            response.setStatus(rental.getStatus().name());
            response.setWheelchairType(wheelchair.getType().name());
            response.setInstitutionName(institution.getName());
            response.setInstitutionAddress(institution.getAddress());
            response.setInstitutionPhone(institution.getTelNumber());
            response.setInstitutionCode(institution.getInstitutionCode()); // 공공기관 코드 설정

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("현재 대여 내역이 없습니다.");
        }
    }

    //상태별 휠체어 조회
    @GetMapping("/api/wheelchairs")
    public ResponseEntity<List<Wheelchair>> getWheelchairsByStatus(@RequestParam("status") String status) {
        List<Wheelchair> wheelchairs;
        if (status.equalsIgnoreCase("ALL")) {
            wheelchairs = wheelchairService.getAllWheelchairs(); // 전체 목록
        } else {
            WheelchairStatus wheelchairStatus = WheelchairStatus.valueOf(status.toUpperCase());
            wheelchairs = wheelchairService.findByStatus(wheelchairStatus); // 상태별 목록
        }
        return ResponseEntity.ok(wheelchairs);
    }


}
