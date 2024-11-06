package com.example.chairman_server.controller.wheelchair;

import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.service.wheelchair.WheelchairService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wheelchairs")
public class WheelchairController {

    private final WheelchairService wheelchairService;
    private final WheelchairRepository wheelchairRepository;

    public WheelchairController(WheelchairService wheelchairService, WheelchairRepository wheelchairRepository) {
        this.wheelchairService = wheelchairService;
        this.wheelchairRepository = wheelchairRepository;
    }

    @GetMapping
    public List<Wheelchair> getWheelchairsByStatus(@RequestParam(required = false) String status) {
        if ("ALL".equalsIgnoreCase(status)) {
            return wheelchairService.getAllWheelchairs(); // 모든 휠체어를 반환
        } else {
            return wheelchairService.findByStatus(WheelchairStatus.valueOf(status)); // 상태별 휠체어 반환
        }
    }

    @GetMapping("/count")
    public Map<String, Integer> getWheelchairCounts() {
        int total = wheelchairService.countAll();
        int available = wheelchairService.countByStatus(WheelchairStatus.AVAILABLE);
        int broken = wheelchairService.countByStatus(WheelchairStatus.BROKEN);
        int rented = wheelchairService.countByStatus(WheelchairStatus.RENTED);
        int waiting = wheelchairService.countByStatus(WheelchairStatus.WAITING);

        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", total);
        counts.put("available", available);
        counts.put("broken", broken);
        counts.put("rented", rented);
        counts.put("waiting", waiting);

        return counts;
    }
}