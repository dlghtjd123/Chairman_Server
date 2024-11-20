package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;  // InstitutionRepository 주입 추가

    public User getUser(Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new IllegalArgumentException("없는 유저 입니다.");
        }
    }

    // 특정 기관의 대시보드 데이터 가져오기
    public InstitutionData getInstitutionData(Long institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다."));
        return new InstitutionData(
                institution.getInstitutionId(),
                institution.getName(),
                institution.getTelNumber(),
                institution.getInstitutionCode()
        );
    }

    // 모든 공공기관 조회
    public List<InstitutionData> getAllInstitutions() {
        List<Institution> institutions = institutionRepository.findAll();
        return institutions.stream()
                .map(institution -> new InstitutionData(
                        institution.getInstitutionId(),
                        institution.getName(),
                        institution.getTelNumber(),
                        institution.getInstitutionCode()))
                .collect(Collectors.toList());
    }

    public void clear() {
        userRepository.deleteAll();
    }
}
