package com.example.chairman_server.service.institution;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public InstitutionService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public Institution findByInstitutionCode(Long institutionCode) {
        return institutionRepository.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new RuntimeException("기관을 찾을 수 없습니다."));
    }

    // 변환 메서드 추가
    private InstitutionData convertToInstitutionData(Institution institution) {
        InstitutionData institutionData = new InstitutionData();
        institutionData.setId(institution.getInstitutionId());
        institutionData.setName(institution.getName());
        institutionData.setInstitutionCode(institution.getInstitutionCode());
        institutionData.setAddress(institution.getAddress());
        institutionData.setTelNumber(institution.getTelNumber());
        return institutionData;
    }

}
