package com.example.chairman_server.domain.Institution;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String TelNumber;

    @Column(unique = true, nullable = false)
    private String institutionCode; // 공공기관 고유 코드


    // 필요 시 다른 정보 추가
}
