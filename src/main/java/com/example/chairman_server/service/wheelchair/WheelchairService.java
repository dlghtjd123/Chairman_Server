package com.example.chairman_server.service.wheelchair;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WheelchairService {

    @Autowired
    private WheelchairRepository wheelchairRepository;

    //상태에 따른 휠체어 목록 반환
    public List<Wheelchair> findByStatus(WheelchairStatus status) {
        return wheelchairRepository.findByStatus(status);
    }
    // 전체 휠체어 목록 반환
    public List<Wheelchair> getAllWheelchairs() {
        return wheelchairRepository.findAll();
    }
    @Autowired
    public WheelchairService(WheelchairRepository wheelchairRepository) {
        this.wheelchairRepository = wheelchairRepository;
    }

    public int countAll() {
        return (int) wheelchairRepository.count();
    }

    public int countByStatus(WheelchairStatus status) {
        return wheelchairRepository.countByStatus(status);
    }

    public Map<String, Integer> getGlobalWheelchairCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", countAll());
        counts.put("available", countByStatus(WheelchairStatus.AVAILABLE));
        counts.put("broken", countByStatus(WheelchairStatus.BROKEN));
        counts.put("rented", countByStatus(WheelchairStatus.RENTED));
        counts.put("waiting", countByStatus(WheelchairStatus.WAITING));
        return counts;
    }

    // 특정 기관의 휠체어 상태별 개수 조회 메서드 추가
    public Map<String, Integer> getWheelchairCountsByInstitution(Institution institution) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("available", wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.AVAILABLE));
        counts.put("broken", wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.BROKEN));
        counts.put("rented", wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.RENTED));
        counts.put("waiting", wheelchairRepository.countByInstitutionAndStatus(institution, WheelchairStatus.WAITING));
        return counts;
    }
}