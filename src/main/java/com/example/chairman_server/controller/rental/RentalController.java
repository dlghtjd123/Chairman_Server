package com.example.chairman_server.controller.rental;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.rental.RentalRequest;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.user.UserRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.service.rental.RentalService;
import com.example.chairman_server.service.wheelchair.WheelchairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    //대여 요청 메서드
    @PostMapping("/rent")
    public ResponseEntity<Rental> rentWheelchair(@RequestBody RentalRequest rentalRequest) {
        Rental rental = rentalService.rentWheelchair(rentalRequest.getUsername(), rentalRequest.getWheelchairType(), LocalDateTime.parse(rentalRequest.getReturnDate()));
        return ResponseEntity.ok(rental);
    }

    // 대여 요청 승인 메서드
    @PutMapping("/approve/{rentalId}")
    public ResponseEntity<Rental> approveRental(@PathVariable Long rentalId) {
        Rental rental = rentalService.approveRental(rentalId);
        return ResponseEntity.ok(rental);
    }
    //대여 요청 거절 메서드
    @PutMapping("/reject/{rentalId}")
    public ResponseEntity<Rental> rejectRental(@PathVariable Long rentalId) {
        Rental rental = rentalService.rejectRental(rentalId);
        return ResponseEntity.ok(rental);
    }

    @PutMapping("/return")
    @Transactional
    public Rental returnWheelchair(@RequestParam String username) {
        // 로그인한 유저 정보 가져오기
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 해당 사용자의 현재 대여 정보 가져오기
        Rental rental = rentalRepository.findCurrentRentalByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록을 찾을 수 없습니다."));

        // 이미 반납된 경우 예외 처리
        if ( rental.getStatus() == RentalStatus.RETURNED) {
            throw new IllegalStateException("이미 반납된 대여입니다.");
        }

        // 대여된 휠체어 상태를 "AVAILABLE"로 변경
        Wheelchair wheelchair = rental.getWheelchair();
        wheelchair.changeStatus(WheelchairStatus.AVAILABLE);
        wheelchairRepository.save(wheelchair);

        // 대여 상태를 "RETURNED"로 변경
        rental.changeStatus(RentalStatus.RETURNED);
        return rentalRepository.save(rental);
    }

    // 휠체어 대여 취소하기
    @PostMapping("/cancel")
    public ResponseEntity<Rental> cancelWheelchair(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        System.out.println("Cancel request received");
        String token = authorizationHeader.substring(7); // "Bearer " 제거
        String username;

        try {
            username = jwtUtil.extractUsername(token); // JWT에서 사용자 이름 추출
        } catch (Exception e) {
            // JWT 처리 중 오류 발생 시
            System.out.println("JWT 처리 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Rental rental = rentalService.cancelWheelchair(username);
            return ResponseEntity.ok(rental);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 대여 기록을 찾지 못했을 경우
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 이미 반납된 경우
        }
    }

    // 대기 중인 대여 목록 조회 메서드
    @GetMapping("/list")
    public ResponseEntity<List<Rental>> getWaitingRentals() {
        List<Rental> rentals = rentalService.getRentalsByStatus(RentalStatus.WAITING);
        return ResponseEntity.ok(rentals);
    }

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
