package com.example.chairman_server.repository.wheelchair;

import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.example.chairman_server.domain.wheelchair.WheelchairStatus;
import com.example.chairman_server.domain.wheelchair.WheelchairType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface WheelchairRepository extends JpaRepository<Wheelchair, Long> {
    long countByStatus(String status);

    int countByStatus(WheelchairStatus status);

    Optional<Wheelchair> findFirstByTypeAndStatus(WheelchairType type, WheelchairStatus status);

    List<Wheelchair> findByStatus(WheelchairStatus status);
}