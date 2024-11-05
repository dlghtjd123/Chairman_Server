package com.example.chairman_server.dto.user;

import com.example.chairman_server.domain.user.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserCreateRequest {

    @Size(min = 3, max = 25)
    @NotEmpty(message = "사용자ID는 필수항목입니다.")
    private String username;
    
    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    private String password;

    @NotEmpty(message = "전화번호는 필수항목입니다.")
    private String phoneNumber;

    @NotNull(message = "사용자 역할은 필수항목입니다.")
    private UserRole role;

    private String guardianCode;    // 보호자 모드일 경우 보호자 코드를 입력 (optional)

    
}
