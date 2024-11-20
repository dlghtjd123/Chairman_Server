package com.example.chairman_server.service.rental;

import com.example.chairman_server.config.JwtUtil;
import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.user.UserRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        boolean isAlreadyRenting = rentalRepository.findByUserAndStatus(user, RentalStatus.ACTIVE).isPresent();
        if (isAlreadyRenting) {
            throw new IllegalStateException("현재 이미 휠체어를 빌리고 있습니다.");
        }

        // 대여 가능한 휠체어 검색
        Wheelchair wheelchair = wheelchairRepository.findFirstByInstitutionAndTypeAndStatus(
                        institution, wheelchairType, WheelchairStatus.AVAILABLE)
                .orElseThrow(() -> new IllegalArgumentException("대여 가능한 휠체어가 없습니다."));

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
    public Rental approveRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental ID"));

        rental.setStatus(RentalStatus.ACTIVE);
        rental.getWheelchair().changeStatus(WheelchairStatus.RENTED);

        wheelchairRepository.save(rental.getWheelchair());
        return rentalRepository.save(rental);
    }

    // 대여 거절
    @Transactional
    public Rental rejectRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental ID"));

        rental.setStatus(RentalStatus.NORMAL);
        rental.getWheelchair().changeStatus(WheelchairStatus.AVAILABLE);

        wheelchairRepository.save(rental.getWheelchair());
        return rentalRepository.save(rental);
    }

    // 대여 취소
    @Transactional
    public Rental cancelWheelchair(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Rental rental = rentalRepository.findCurrentRentalByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록을 찾을 수 없습니다."));

        rental.getWheelchair().changeStatus(WheelchairStatus.AVAILABLE);
        rental.setStatus(RentalStatus.NORMAL);

        wheelchairRepository.save(rental.getWheelchair());
        return rentalRepository.save(rental);
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

    @Transactional(readOnly = true)
    public List<LocalDate> findAvailableDates(Long institutionCode, WheelchairType wheelchairType) {
        // institutionCode로 Institution 조회
        Institution institution = institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다."));

        // 대여 가능한 휠체어 조회
        List<Wheelchair> availableWheelchairs = wheelchairRepository.findByInstitutionAndTypeAndStatus(
                institution, wheelchairType, WheelchairStatus.AVAILABLE);

        // 대여 가능한 날짜를 저장할 Set (중복 제거)
        Set<LocalDate> availableDates = new HashSet<>();

        for (Wheelchair wheelchair : availableWheelchairs) {
            // 각 휠체어의 대여 가능한 기간을 계산하여 추가
            List<Rental> rentals = rentalRepository.findByWheelchairAndStatus(wheelchair, RentalStatus.ACTIVE);

            LocalDate today = LocalDate.now();
            LocalDate endOfBookingWindow = today.plusMonths(1); // 1개월 뒤까지 대여 가능 날짜 계산

            while (!today.isAfter(endOfBookingWindow)) {
                LocalDate finalToday = today;
                boolean isAvailable = rentals.stream().noneMatch(rental ->
                        (rental.getRentalDate().toLocalDate().isBefore(finalToday.plusDays(1)) &&
                                rental.getReturnDate().toLocalDate().isAfter(finalToday.minusDays(1)))
                );

                if (isAvailable) {
                    availableDates.add(today);
                }
                today = today.plusDays(1);
            }
        }

        // List로 반환
        return new ArrayList<>(availableDates);
    }

    // JWT에서 이메일 추출
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

}
