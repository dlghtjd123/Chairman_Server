package com.example.chairman_server.domain.wheelchair;

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

    public Wheelchair(WheelchairStatus status, WheelchairType type) {
        this.status = status;
        this.type = type;
    }


    public void changeStatus(WheelchairStatus newStatus){
        status=newStatus;
    }
}