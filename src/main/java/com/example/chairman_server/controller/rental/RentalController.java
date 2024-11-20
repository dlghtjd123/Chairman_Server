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

    // 휠체어 대여 요청
    @PostMapping("/{institutionCode}/rent")
    public ResponseEntity<Rental> rentWheelchair(
            @PathVariable Long institutionCode,
            @RequestBody RentalRequest rentalRequest) {

        Rental rental = rentalService.rentWheelchair(
                institutionCode,
                rentalRequest.getEmail(),
                rentalRequest.getWheelchairType(),
                LocalDateTime.parse(rentalRequest.getRentalDate()),
                LocalDateTime.parse(rentalRequest.getReturnDate())
        );

        return ResponseEntity.ok(rental);
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

    // 대여 취소
    @PostMapping("/{institutionCode}/cancel")
    public ResponseEntity<Rental> cancelWheelchair(
            @PathVariable Long institutionCode,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7); // "Bearer " 제거
        String email = rentalService.extractEmailFromToken(token);

        Rental rental = rentalService.cancelWheelchair(email);
        return ResponseEntity.ok(rental);
    }

    // 대기 중인 대여 요청 목록 조회
    @GetMapping("/{institutionCode}/list")
    public ResponseEntity<List<Rental>> getWaitingRentals(@PathVariable Long institutionCode) {
        List<Rental> rentals = rentalService.getRentalsByStatusAndInstitution(
                RentalStatus.WAITING, institutionCode);
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
