package com.example.chairman_server.service.wheelchair;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.dto.wheelchair.WheelchairDetailResponse;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
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

        // WheelchairDetailResponse로 변환
        return wheelchairs.stream()
                .map(wheelchair -> {
                    WheelchairDetailResponse response = new WheelchairDetailResponse();
                    response.setWheelchairId(wheelchair.getWheelchairId());
                    response.setStatus(wheelchair.getStatus().name());
                    response.setType(wheelchair.getType().name());

                    // RENTED나 BROKEN 상태일 때 사용자 정보 포함
                    if (wheelchair.getStatus() == WheelchairStatus.RENTED || wheelchair.getStatus() == WheelchairStatus.WAITING) {
                        if (wheelchair.getUser() != null) {
                            response.setUserName(wheelchair.getUser().getName());
                            response.setUserPhone(wheelchair.getUser().getPhoneNumber());
                        }
                    }

                    return response;
                })
                .collect(Collectors.toList());
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