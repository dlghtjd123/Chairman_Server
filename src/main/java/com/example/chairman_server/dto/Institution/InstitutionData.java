package com.example.chairman_server.dto.Institution;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstitutionData {
    private Long id;
    private String name;
    private String telNumber;
    private Long institutionCode;
    private String address;

    public InstitutionData(Long id, String name, String telNumber, Long institutionCode, String address) {
        this.id = id;
        this.name = name;
        this.telNumber = telNumber;
        this.institutionCode = institutionCode;
        this.address = address;
    }
}
