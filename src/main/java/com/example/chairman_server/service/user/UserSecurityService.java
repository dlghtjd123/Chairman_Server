package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.user.UserRole;
import com.example.chairman_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    // 의존성 주입을 통한 UserRepository 선언
    private final UserRepository userRepository;

    // 사용자의 이름(username)을 기반으로 사용자 정보를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Trying to authenticate user: " + username); // 로그 추가
        // 사용자 정보를 데이터베이스에서 가져오고, 없을 경우 예외를 던짐
        com.example.chairman_server.domain.user.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 사용자 권한을 담을 리스트 생성
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 사용자의 역할에 따른 권한 설정
        if (user.getRole().equals(UserRole.ADMIN)) {
            // 관리자 권한 부여
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.name()));
        } else if (user.getRole().equals(UserRole.NORMAL)) {
            // 일반 사용자 권한 부여
            authorities.add(new SimpleGrantedAuthority(UserRole.NORMAL.name()));
        }

        // Spring Security의 UserDetails를 반환하여 인증 시스템에서 사용할 수 있도록 함
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
