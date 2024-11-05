package com.example.chairman_server.repository.rental;

import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    Optional<Rental> findByRentalIdAndUser(Long rentalId, User user);

    @Query("SELECT r FROM Rental r WHERE r.user = ?1 AND r.status = com.example.chairman_server.domain.rental.RentalStatus.ACTIVE")
    Optional<Rental> findCurrentRentalByUser(User user);

    // 현재 대여 중인 기록을 가져오는 메서드
    List<Rental> findByUser(User user);

    List<Rental> findByStatus(RentalStatus status); // RentalStatus를 기준으로 조회하는 메서드 추가
}