package com.example.chairman_server.repository.Institution;

import com.example.chairman_server.domain.Institution.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByInstitutionCode(Long institutionId);
}
