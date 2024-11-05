package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import com.example.chairman_server.dto.rental.RentalRequest;
import com.example.chairman_server.service.rental.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/normal")
public class NormalController {

    private final RentalService rentalService;
    private final JwtUtil jwtUtil;

    // 일반 사용자 페이지 환영 메시지
    @GetMapping
    public ResponseEntity<String> normalPage() {
        return ResponseEntity.ok("Welcome to the normal user page");
    }

    // 대여 폼 보여주기 (휠체어 종류 반환)
    @GetMapping("/rent")
    public ResponseEntity<List<WheelchairType>> showRentForm() {
        return ResponseEntity.ok(Arrays.asList(WheelchairType.values()));
    }

    // 휠체어 대여 처리
    @PostMapping("/rent")
    public ResponseEntity<Rental> rentWheelchair(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody RentalRequest rentRequest // JSON 요청 본문을 처리
    ) {

        System.out.println("Authorization Header: " + authorizationHeader);
        System.out.println("Wheelchair Type: " + rentRequest.getWheelchairType());
        System.out.println("Return Date: " + rentRequest.getReturnDate());

        String token = authorizationHeader.substring(7); // "Bearer " 제거
        String username = jwtUtil.extractUsername(token); // JWT에서 사용자 이름 추출
        LocalDateTime returnDateTime = LocalDateTime.parse(rentRequest.getReturnDate());
        
        Rental rental = rentalService.rentWheelchair(username, rentRequest.getWheelchairType(), returnDateTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    // 휠체어 반납 처리
    @PostMapping("/return")
    public ResponseEntity<Rental> returnWheelchair(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
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
            Rental rental = rentalService.returnWheelchair(username);
            return ResponseEntity.ok(rental);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 대여 기록을 찾지 못했을 경우
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 이미 반납된 경우
        }
    }

}