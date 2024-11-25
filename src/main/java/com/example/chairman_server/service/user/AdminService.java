package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final InstitutionRepository institutionRepository;
    private final RentalRepository rentalRepository;
    private final WheelchairRepository wheelchairRepository;

    @Autowired
    public AdminService(InstitutionRepository institutionRepository,
                                   RentalRepository rentalRepository,
                                   WheelchairRepository wheelchairRepository) {
        this.institutionRepository = institutionRepository;
        this.rentalRepository = rentalRepository;
        this.wheelchairRepository = wheelchairRepository;
    }

    public Institution loginByCode(Long code) {
        System.out.println("입력된 기관 코드: " + code); // 입력된 코드 로그 출력

        return institutionRepository.findByInstitutionCode(code)
                .orElseThrow(() -> new IllegalArgumentException("해당 코드의 기관을 찾을 수 없습니다."));
    }


    // 요청 승인
    @Transactional
    public Rental approveRental(Long rentalId, Long institutionCode) {
        // rentalId로 대여 요청 조회
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental ID"));

        // 요청이 해당 기관 코드에 속하는지 확인
        if (!rental.getInstitution().getInstitutionCode().equals(institutionCode)) {
            throw new IllegalArgumentException("Rental does not belong to the given institution");
        }

        // 상태가 "WAITING"인지 확인
        if (!rental.getStatus().equals(RentalStatus.WAITING)) {
            throw new IllegalArgumentException("Only waiting rentals can be approved");
        }

        // 대여 상태를 "ACTIVE"로 변경
        rental.setStatus(RentalStatus.ACTIVE);
        rentalRepository.save(rental);

        // 휠체어 상태를 "RENTED"로 변경
        Wheelchair wheelchair = rental.getWheelchair();
        wheelchair.changeStatus(WheelchairStatus.RENTED);
        wheelchairRepository.save(wheelchair); // 휠체어 상태 저장

        // 로그 추가
        System.out.println("Rental approved: " + rental.getRentalId());

        return rental;
    }

    // 요청 거절
    @Transactional
    public Rental rejectRental(Long rentalId, Long institutionCode) {
        // rentalId로 대여 요청 조회
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental ID"));

        // 요청이 해당 기관 코드에 속하는지 확인
        if (!rental.getInstitution().getInstitutionCode().equals(institutionCode)) {
            throw new IllegalArgumentException("Rental does not belong to the given institution");
        }

        // 상태가 "WAITING"인지 확인
        if (!rental.getStatus().equals(RentalStatus.WAITING)) {
            throw new IllegalArgumentException("Only waiting rentals can be rejected");
        }

        // 휠체어 상태를 "AVAILABLE"로 변경
        Wheelchair wheelchair = rental.getWheelchair();
        wheelchair.setStatus(WheelchairStatus.AVAILABLE);
        wheelchairRepository.save(wheelchair);

        // 대여 요청 삭제
        rentalRepository.delete(rental);

        // 로그 추가
        System.out.println("Rental rejected: " + rental.getRentalId());

        return rental;
    }


    // 휠체어 상태별 통계 조회
    public Map<String, Long> getWheelchairStatusCounts() {
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("AVAILABLE", wheelchairRepository.countByStatus("AVAILABLE"));
        statusCounts.put("RENTED", wheelchairRepository.countByStatus("RENTED"));
        statusCounts.put("BROKEN", wheelchairRepository.countByStatus("BROKEN"));
        return statusCounts;
    }

    // 관리자가 전체 대여 목록을 조회
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    // InstitutionCode로 Institution 조회
    public Institution findInstitutionByCode(Long institutionCode) {
        return institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다. institutionCode: " + institutionCode));
    }

}
