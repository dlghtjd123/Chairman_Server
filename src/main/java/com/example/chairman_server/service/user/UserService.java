package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.user.UserRole;
import com.example.chairman_server.dto.user.LoginRequest;
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
public class UserService {
        
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }


    public User create(UserCreateRequest request) {
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getName(),  // name 필드를 포함하여 생성자 호출
                request.getAddress(),
                request.isAdmin() ? UserRole.ADMIN : UserRole.NORMAL
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


    public Optional<User> findByEmailWithRentals(String email) {
        return userRepository.findByEmailWithRentals(email);
    }

}
