package com.example.chairman_server.controller.wheelchair;

import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.repository.wheelchair.WheelchairRepository;
import com.example.chairman_server.service.wheelchair.WheelchairService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wheelchair")
public class WheelchairController {

    private final WheelchairService wheelchairService;
    private final WheelchairRepository wheelchairRepository;

    public WheelchairController(WheelchairService wheelchairService, WheelchairRepository wheelchairRepository) {
        this.wheelchairService = wheelchairService;
        this.wheelchairRepository = wheelchairRepository;
    }

    /*@GetMapping("/{institutionCode}")
    public ResponseEntity<List<Wheelchair>> getWheelchairsByInstitution(@PathVariable Long institutionId) {
        List<Wheelchair> wheelchairs = wheelchairService.findByInstitution(institutionId);
        return ResponseEntity.ok(wheelchairs);
    }*/

    @GetMapping
    public List<Wheelchair> getWheelchairsByStatus(@RequestParam(required = false) String status) {
        if ("ALL".equalsIgnoreCase(status)) {
            return wheelchairService.getAllWheelchairs(); // 모든 휠체어를 반환
        } else {
            return wheelchairService.findByStatus(WheelchairStatus.valueOf(status)); // 상태별 휠체어 반환
        }
    }
}