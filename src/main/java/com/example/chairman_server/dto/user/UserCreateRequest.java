package com.example.chairman_server.dto.user;

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

    @Size(min = 3, max = 50)
    @NotEmpty(message = "이메일은 필수항목입니다.")
    private String email;

    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    private String password;

    @NotEmpty(message = "이름은 필수항목입니다.")
    private String name;

    @NotEmpty(message = "전화번호는 필수항목입니다.")
    private String phoneNumber;

    private String address;

    @NotNull(message = "관리자 여부는 필수항목입니다.")
    private boolean isAdmin;  // 관리자 모드 여부 추가

    //동의서 3개 추가
    @NotNull(message="이용약관 동의 여부는 필수항목입니다.")
    private boolean agreeTerms;
    @NotNull(message="개인정보 처리방침 동의 여부는 필수항목입니다.")
    private boolean agreePrivacy;
    @NotNull(message="제3자 정보 제공 동의 여부는 필수항목입니다.")
    private boolean agreeThirdParty;
}
