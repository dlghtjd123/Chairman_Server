package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.user.UserRole;
import com.example.chairman_server.dto.user.UserCreateRequest;
import com.example.chairman_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// 공통 로직 회원가입 같은 것들 모음

@RequiredArgsConstructor
@Service
public class NormalService {
        
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }


    public User create(UserCreateRequest request) {

        // 입력값 유효성 검사
        if (request == null) {
            throw new IllegalArgumentException("회원가입 요청이 null입니다.");
        }

        // 이메일 중복 확인
        if (isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("중복된 이메일입니다: " + request.getEmail());
        }

        // 전화번호 중복 확인
        if (isPhoneNumberExists(request.getPhoneNumber())) {
            throw new IllegalArgumentException("중복된 전화번호입니다: " + request.getPhoneNumber());
        }
        // 사용자 생성
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getName(),
                request.getAddress(),
                request.isAdmin() ? UserRole.ADMIN : UserRole.USER,
                request.isAgreeTerms(),
                request.isAgreePrivacy(),
                request.isAgreeThirdParty()
        );
        this.userRepository.save(user);
        return user;
    }

    @Transactional
    public Authentication authenticate(String email, String password) {
        // UsernamePasswordAuthenticationToken을 사용하여 이메일로 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        System.out.println("Authentication: " + authentication.isAuthenticated());
        return authentication;
    }


    public boolean isEmailExists(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일이 유효하지 않습니다.");
        }
        boolean exists = userRepository.existsByEmail(email);
        System.out.println("이메일 중복 확인 [" + email + "] : " + exists);
        return exists;
    }

    public boolean isPhoneNumberExists(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("이메일이 유효하지 않습니다.");
        }
        boolean exists = userRepository.existsByPhoneNumber(phoneNumber);
        System.out.println("전화번호 중복 확인 [" + phoneNumber + "] : " + exists);
        return exists;
    }
}
