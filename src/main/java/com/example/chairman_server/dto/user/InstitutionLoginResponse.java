package com.example.chairman_server.dto.user;

import com.example.chairman_server.domain.Institution.Institution;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstitutionLoginResponse {
    private String token;
    private Institution institution;
}
