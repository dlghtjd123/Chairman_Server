package com.example.chairman_server.service.wheelchair;

import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}