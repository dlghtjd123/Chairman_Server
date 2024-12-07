package com.example.chairman_server.controller.user;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.dto.user.UserUpdateRequest;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.user.UserRepository;
import com.example.chairman_server.service.institution.InstitutionService;
import com.example.chairman_server.service.rental.RentalService;
import com.example.chairman_server.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final RentalService rentalService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final InstitutionService institutionService;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    // 일반 사용자 페이지 환영 메시지
    @GetMapping
    public ResponseEntity<String> userPage() {
        return ResponseEntity.ok("Welcome to the user page");
    }

    // 특정 공공기관에서의 대여 가능한 휠체어 개수 조회
    @GetMapping("/{institutionCode}/available-count")
    public ResponseEntity<Map<String, Integer>> getAvailableWheelchairCounts(@PathVariable Long institutionCode) {
        Map<String, Integer> availableCounts = rentalService.getAvailableWheelchairCounts(institutionCode);
        return ResponseEntity.ok(availableCounts);
    }

    // 모든 공공기관 목록 조회
    @GetMapping("/institutions")
    public ResponseEntity<List<InstitutionData>> getAllInstitutions() {
        List<InstitutionData> institutions = userService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }

    @GetMapping("/institutions/{institutionCode}")
    public ResponseEntity<InstitutionData> getInstitutionByCode(@PathVariable Long institutionCode) {
        Institution institution = institutionService.findByInstitutionCode(institutionCode);
        if (institution == null) {
            return ResponseEntity.status(404).build(); // 404 Not Found 반환
        }

        // Institution -> InstitutionData 변환
        InstitutionData institutionData = new InstitutionData(
                institution.getInstitutionId(),
                institution.getName(),
                institution.getTelNumber(),
                institution.getInstitutionCode(),
                institution.getAddress()
        );

        return ResponseEntity.ok(institutionData);
    }


    // 사용자 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getRentalInfo(@RequestHeader("Authorization") String authorizationHeader) {
        // JWT 토큰에서 이메일 추출
        String token = authorizationHeader.substring(7);  // "Bearer "를 제외한 토큰 부분
        String email = jwtUtil.extractEmail(token);

        // 사용자 정보 가져오기
        Map<String, String> userInfo = userService.getUserInfo(email);

        if (userInfo.containsKey("error")) {
            return ResponseEntity.status(404).body(userInfo.get("error"));
        } else {
            return ResponseEntity.ok(userInfo);
        }
    }

    // 사용자 정보 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String authorizationHeader, @RequestBody UserUpdateRequest updateRequest) {
        String token = authorizationHeader.substring(7);  // "Bearer " 제거
        String email = jwtUtil.extractEmail(token);  // 이메일 추출

        // 사용자 정보 수정
        userService.updateUserInfo(email, updateRequest.getName(), updateRequest.getPhoneNumber(), updateRequest.getAddress());

        return ResponseEntity.ok("사용자 정보가 성공적으로 수정되었습니다.");
    }

    //사용자 프로필 사진 업로드
    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestHeader("Authorization") String authorizationHeader,
                                                @RequestParam("photo") MultipartFile photo) {
        String token = authorizationHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        try {
            //프로필 사진 저장 처리
            String photoUrl = userService.saveProfilePhoto(email, photo);

            return ResponseEntity.ok(Map.of("message","프로필 사진이 성공적으로 업로드되었습니다.", "photoUrl", photoUrl));
        } catch (IOException e) {
            log.error("프로필 사진 업로드 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body("프로필 사진 업로드에 실패했습니다.");
        }
    }

}