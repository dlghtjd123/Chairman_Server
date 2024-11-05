package com.example.chairman_server.service.Institution;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public Institution loginByCode(String institutionCode) {
        return institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 코드의 기관을 찾을 수 없습니다."));
    }
}
