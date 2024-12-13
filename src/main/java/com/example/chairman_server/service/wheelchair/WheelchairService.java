package com.example.chairman_server.service.wheelchair;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.wheelchair.WheelchairDetailResponse;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.repository.rental.RentalRepository;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WheelchairService {

    @Autowired
    private WheelchairRepository wheelchairRepository;
    private final InstitutionRepository institutionRepository;
    private RentalRepository rentalRepository;

    //상태에 따른 휠체어 목록 반환
    public List<Wheelchair> findByStatus(WheelchairStatus status) {
        return wheelchairRepository.findByStatus(status);
    }
    // 전체 휠체어 목록 반환
    public List<Wheelchair> getAllWheelchairs() {
        return wheelchairRepository.findAll();
    }

    @Autowired
    public WheelchairService(WheelchairRepository wheelchairRepository, InstitutionRepository institutionRepository) {
        this.wheelchairRepository = wheelchairRepository;
        this.institutionRepository = institutionRepository;
    }

    public Institution validateInstitution(Long institutionCode) {
        return institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid institution code."));
    }

    public List<WheelchairDetailResponse> getWheelchairDetailsByInstitutionAndStatus(Long institutionCode, String status) {
        List<Wheelchair> wheelchairs = wheelchairRepository.findAllByInstitutionInstitutionCode(institutionCode);

        return wheelchairs.stream()
                .filter(wheelchair -> wheelchair.getStatus().toString().equalsIgnoreCase(status))
                .map(wheelchair -> {
                    WheelchairDetailResponse response = new WheelchairDetailResponse();
                    response.setWheelchairId(wheelchair.getWheelchairId());
                    response.setType(wheelchair.getType().toString());
                    response.setWheelchairStatus(wheelchair.getStatus().toString());

                    // 대여 상태 추가
                    Rental activeRental = rentalRepository.findByWheelchairAndStatus(wheelchair, RentalStatus.ACTIVE).orElse(null);
                    if (activeRental != null) {
                        response.setRentalStatus(activeRental.getStatus().toString());
                    }

                    // 대여자 정보 추가
                    if (wheelchair.getUser() != null) {
                        response.setUserName(wheelchair.getUser().getName());
                        response.setUserPhone(wheelchair.getUser().getPhoneNumber());
                    }

                    return response;
                }).collect(Collectors.toList());
    }


    public List<WheelchairDetailResponse> getDetailsByStatus(Long institutionCode, String status) {
        Institution institution = validateInstitution(institutionCode); // 기관 검증
        List<Wheelchair> wheelchairs;

        if ("ALL".equalsIgnoreCase(status)) {
            // 모든 상태의 휠체어 조회
            wheelchairs = wheelchairRepository.findByInstitution(institution);
        } else {
            // 특정 상태로 휠체어 조회
            WheelchairStatus wheelchairStatus = WheelchairStatus.valueOf(status.toUpperCase());
            wheelchairs = wheelchairRepository.findByInstitutionAndStatus(institution, wheelchairStatus);
        }

        return wheelchairs.stream()
                .map(wheelchair -> {
                    WheelchairDetailResponse response = new WheelchairDetailResponse();
                    response.setWheelchairId(wheelchair.getWheelchairId());
                    response.setWheelchairStatus(wheelchair.getStatus().name());
                    response.setType(wheelchair.getType().name());

                    // 대여 상태 설정
                    Rental activeRental = rentalRepository.findByWheelchairAndStatus(wheelchair, RentalStatus.ACTIVE).orElse(null);
                    if (activeRental != null) {
                        response.setRentalStatus(activeRental.getStatus().name());
                    }

                    // 대여자 정보 포함
                    if (wheelchair.getUser() != null) {
                        response.setUserName(wheelchair.getUser().getName());
                        response.setUserPhone(wheelchair.getUser().getPhoneNumber());
                    }

                    return response;
                }).collect(Collectors.toList());
    }

    public Map<String, Long> getStatusCounts(Long institutionCode) {
        Institution institution = validateInstitution(institutionCode); // 기관 검증

        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("AVAILABLE", (long) wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.AVAILABLE));
        statusCounts.put("RENTED", (long) wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.RENTED));
        statusCounts.put("BROKEN", (long) wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.BROKEN));
        statusCounts.put("WAITING", (long) wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.WAITING));
        statusCounts.put("ACCEPTED", (long) wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.ACCEPTED));

        return statusCounts;
    }
}