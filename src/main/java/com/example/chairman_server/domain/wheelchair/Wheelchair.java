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

    @Embedded
    @Column(nullable = true)
    private Location location = new Location(0, 0); // TODO => POINT 타입을 String으로 매핑
    

    @OneToMany(mappedBy = "wheelchair", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Rental> rentals = new ArrayList<>();

    public Wheelchair(WheelchairStatus status, WheelchairType type, Location location) {
        this.status = status;
        this.type = type;
        this.location = location;
    }


    public void changeStatus(WheelchairStatus newStatus){
        status=newStatus;
    }
}