package com.example.chairman_server.domain.user;

import com.example.chairman_server.domain.rental.Rental;
import com.example.chairman_server.domain.rental.RentalStatus;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @OneToMany(mappedBy = "user")
    private List<Rental> rentals;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String address;

    private String name;

    @Enumerated(EnumType.STRING)
    private RentalStatus status = RentalStatus.NORMAL;

    //동의서 관련 필드 추가
    @Column(nullable = false)
    private Boolean agreeTerms; //이용약관 동의

    @Column(nullable = false)
    private Boolean agreePrivacy; //개인정보 처리방침 동의

    @Column(nullable = false)
    private Boolean agreeThirdParty; //제3자 정보 제공 동의

    //프로필 이미지 URL 필드 추가
    @Column(nullable = false)
    private String profileImageUrl;

    public User() {
    }

    public User(String email, String password, String phoneNumber, String name, String address, UserRole role, Boolean agreeTerms, Boolean agreePrivacy, Boolean agreeThirdParty) {
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.address = address;
        this.role = role;
        this.agreeTerms = false;
        this.agreePrivacy = false;
        this.agreeThirdParty = false;
    }

    public User(String email, String password, String phoneNumber, String name, String address, UserRole role, Boolean agreeTerms, Boolean agreePrivacy, Boolean agreeThirdParty, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.address = address;
        this.role = role;
        this.agreeTerms = false;
        this.agreePrivacy = false;
        this.agreeThirdParty = false;
        this.profileImageUrl = profileImageUrl;
    }
}
