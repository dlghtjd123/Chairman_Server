package com.example.chairman_server.repository.wheelchair;

import com.example.chairman_server.domain.Institution.Institution;
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

    //available 중 하나 가져오기 --> rent에 사용
    Optional<Wheelchair> findFirstByInstitutionAndTypeAndStatus(Institution institution, WheelchairType type, WheelchairStatus status);

    // 사용 가능한 휠체어 목록 가져오기
    List<Wheelchair> findByInstitutionAndTypeAndStatus(Institution institution, WheelchairType type, WheelchairStatus status);

    List<Wheelchair> findByStatus(WheelchairStatus status);

    List<Wheelchair> findAllByInstitutionInstitutionCode(Long institutionCode);

    // 특정 Institution, Type, Status 기준으로 휠체어 개수 조회
    int countByInstitutionAndTypeAndStatus(Institution institution, WheelchairType type, WheelchairStatus status);

    // 특정 기관의 휠체어 상태별 조회
    List<Wheelchair> findByInstitutionAndStatus(Institution institution, WheelchairStatus status);

    // 특정 기관의 전체 휠체어 개수 조회
    int countByInstitution(Institution institution);

    // 특정 기관과 상태의 휠체어 개수 조회
    int countByInstitutionAndStatus(Institution institution, WheelchairStatus status);

    // 특정 기관의 모든 휠체어 검색 (상태 무관)
    List<Wheelchair> findByInstitution(Institution institution);
}