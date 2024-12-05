package com.example.chairman_server.repository.rental;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query("SELECT r FROM Rental r WHERE r.user = ?1 AND r.status IN (com.example.chairman_server.domain.rental.RentalStatus.ACTIVE, com.example.chairman_server.domain.rental.RentalStatus.WAITING, com.example.chairman_server.domain.rental.RentalStatus.ACCEPTED)")
    Optional<Rental> findCurrentRentalByUser(User user);

    @Query("SELECT r FROM Rental r WHERE r.status = :status AND r.wheelchair.institution = :institution")
    List<Rental> findByStatusAndWheelchairInstitution(RentalStatus status, Institution institution);

    @Query("SELECT r FROM Rental r WHERE r.user = :user AND r.status = :status")
    Optional<Rental> findByUserAndStatus(User user, RentalStatus status);

    Optional<Rental> findByWheelchairAndStatus(Wheelchair wheelchair, RentalStatus status);
}
