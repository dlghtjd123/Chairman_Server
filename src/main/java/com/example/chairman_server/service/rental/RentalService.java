package com.example.chairman_server.service.rental;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import com.example.chairman_server.dto.rental.WaitingRentalResponse;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.user.UserRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class RentalService {

    private final RentalRepository rentalRepository;
    private final WheelchairRepository wheelchairRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final JwtUtil jwtUtil;

    // 대여 코드 생성
    private String generateRentalCode() {
        return UUID.randomUUID().toString();
    }

    // 대여 처리
    @Transactional
    public Rental rentWheelchair(Long institutionCode, String email, WheelchairType wheelchairType,
                                 LocalDateTime rentalDate, LocalDateTime returnDate) {

        Institution institution = institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 사용자가 이미 대여 중인지 확인
        boolean isAlreadyRenting = rentalRepository.findCurrentRentalByUser(user).isPresent();
        if (isAlreadyRenting) {
            throw new IllegalStateException("사용자가 이미 대여 중입니다.");
        }

        // 대여 가능한 휠체어 검색
        Wheelchair wheelchair = wheelchairRepository.findFirstByInstitutionAndTypeAndStatus(
                        institution, wheelchairType, WheelchairStatus.AVAILABLE)
                .orElseThrow(() -> new IllegalArgumentException("대여 가능한 휠체어가 없습니다."));

        // 사용자와 휠체어 연결
        wheelchair.assignUser(user);

        // 대여 생성
        Rental rental = new Rental(user, wheelchair, rentalDate, returnDate,
                UUID.randomUUID().toString(), RentalStatus.WAITING);

        // 휠체어 상태 변경
        wheelchair.changeStatus(WheelchairStatus.WAITING);
        wheelchairRepository.save(wheelchair);

        return rentalRepository.save(rental);
    }

    // 대여 승인
    @Transactional
    public Rental acceptRental(Long rentalId, Long institutionCode) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental ID"));

        Wheelchair wheelchair = rental.getWheelchair();

        // institutionCode 검증
        if (!wheelchair.getInstitution().getInstitutionCode().equals(institutionCode)) {
            throw new IllegalArgumentException("Invalid institution code for this rental.");
        }

        // WheelchairStatus를 ACCEPTED로 변경
        wheelchair.changeStatus(WheelchairStatus.ACCEPTED);

        // User의 RentalStatus를 ACCEPTED로 변경
        if (rental.getUser() != null) {
            User user = rental.getUser();
            user.setStatus(RentalStatus.ACCEPTED);
            userRepository.save(user); // User 변경사항 저장
        }

        rental.setStatus(RentalStatus.ACCEPTED);
        wheelchairRepository.save(wheelchair); // Wheelchair 변경사항 저장
        return rentalRepository.save(rental); // Rental 변경사항 저장
    }

    @Transactional
    public void rejectRental(Long rentalId, Long institutionCode) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental ID"));

        Wheelchair wheelchair = rental.getWheelchair();

        // institutionCode 검증
        if (!wheelchair.getInstitution().getInstitutionCode().equals(institutionCode)) {
            throw new IllegalArgumentException("Invalid institution code for this rental.");
        }

        // WheelchairStatus를 AVAILABLE로 복원
        wheelchair.changeStatus(WheelchairStatus.AVAILABLE);
        rental.setStatus(RentalStatus.NORMAL);
        wheelchairRepository.save(wheelchair);
        wheelchair.removeUser();

        // Rental 테이블에서 기록 삭제
        rentalRepository.delete(rental);
    }



    @Transactional
    public void cancelWheelchair(String email) {
        // 유저 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 대여 기록 확인
        Rental rental = rentalRepository.findCurrentRentalByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록을 찾을 수 없습니다."));

        // 휠체어 상태 변경 및 사용자 연결 해제
        Wheelchair wheelchair = rental.getWheelchair();
        wheelchair.setStatus(WheelchairStatus.AVAILABLE);
        wheelchair.removeUser();
        wheelchairRepository.save(wheelchair);

        // 대여 기록 삭제
        rentalRepository.delete(rental);
    }


    // 대여 상태와 기관 코드에 따라 대여 목록 조회
    @Transactional(readOnly = true)
    public List<Rental> getRentalsByStatusAndInstitution(RentalStatus status, Long institutionCode) {
        Institution institution = institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다."));

        return rentalRepository.findByStatusAndWheelchairInstitution(status, institution);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getAvailableWheelchairCounts(Long institutionCode) {
        Institution institution = institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다. institutionCode: " + institutionCode));

        log.debug("Institution found: " + institution.getName());

        // 성인용 휠체어 개수
        int adultCount = wheelchairRepository.countByInstitutionAndTypeAndStatus(
                institution, WheelchairType.ADULT, WheelchairStatus.AVAILABLE);

        // 유아용 휠체어 개수
        int childCount = wheelchairRepository.countByInstitutionAndTypeAndStatus(
                institution, WheelchairType.CHILD, WheelchairStatus.AVAILABLE);

        return Map.of(
                "ADULT", adultCount,
                "CHILD", childCount
        );

    }

    // JWT에서 이메일 추출
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

}
