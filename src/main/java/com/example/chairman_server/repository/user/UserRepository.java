package com.example.chairman_server.repository.user;

import com.example.chairman_server.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    //이메일 중복 여부 확인
    boolean existsByEmail(String email);

    //전화번호 중복 여부 확인
    boolean existsByPhoneNumber(String phoneNumber);

}