package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class NormalService {

    private final UserRepository userRepository;
    
    public User getUser(Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new IllegalArgumentException("없는 유저 입니다.");
        }
    }


    public void clear(){
        userRepository.deleteAll();
    }
}
