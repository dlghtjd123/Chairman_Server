package com.example.chairman_server.domain.Institution;

import com.example.chairman_server.domain.wheelchair.Wheelchair;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "institution")
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long institution_Id; // PRIMARY KEY and AUTO_INCREMENT

    private String name;
    private String telNumber;

    @Column(name = "institution_code", unique = true, nullable = false)
    private Long institutionCode;
}

