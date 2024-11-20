package com.example.chairman_server.domain.wheelchair;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.rental.Rental;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@RequiredArgsConstructor
@Entity
public class Wheelchair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wheelchairId;

    @Setter
    @Enumerated(EnumType.STRING)
    private WheelchairStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WheelchairType type;

    @OneToMany(mappedBy = "wheelchair", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Rental> rentals = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "institution_id", nullable = false) // institution_id로 수정
    private Institution institution;

    public Wheelchair(WheelchairStatus status, WheelchairType type) {
        this.status = status;
        this.type = type;
    }

    public void changeStatus(WheelchairStatus newStatus) {
        status = newStatus;
    }
}
