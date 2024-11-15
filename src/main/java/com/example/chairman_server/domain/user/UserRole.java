package com.example.chairman_server.domain.user;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    // 역할을 가져올 수 있는 getter 메서드
    private final String role;

    // Enum 생성자
    UserRole(String role) {
        this.role = role;
    }

    // toString() 메서드 오버라이드
    @Override
    public String toString() {
        return this.role;
    }
}
