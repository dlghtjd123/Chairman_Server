package com.example.chairman_server.domain.Institution;

import com.example.chairman_server.domain.wheelchair.Wheelchair;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "institution")
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "institution_code", unique = true, nullable = false)
    private Long institutionCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "tel_number")
    private String telNumber;
}



