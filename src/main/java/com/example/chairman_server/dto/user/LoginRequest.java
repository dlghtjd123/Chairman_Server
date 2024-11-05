package com.example.chairman_server.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @NotEmpty(message = "사용자ID는 필수항목입니다.")
    private String username;
    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    private String password;
}
