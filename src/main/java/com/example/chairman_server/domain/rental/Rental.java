package com.example.chairman_server.domain.rental;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.domain.wheelchair.Wheelchair;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "rentalId") // Identity-based reference handling
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rentalId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "wheelchair_id", nullable = false)
    private Wheelchair wheelchair;

    @Column(nullable = false)
    private LocalDateTime rentalDate;

    private LocalDateTime returnDate;

    @Column(nullable = false)
    private String rentalCode;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status;

    @ManyToOne
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    public void changeStatus(RentalStatus newStatus) {
        this.status = newStatus;
    }

    // 수정된 생성자
    public Rental(User user, Wheelchair wheelchair, LocalDateTime rentalDate, LocalDateTime returnDate,
                  String rentalCode, RentalStatus status) {
        this.user = user;
        this.wheelchair = wheelchair;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.rentalCode = rentalCode;
        this.status = status;
        this.institution = wheelchair.getInstitution(); // 휠체어의 공공기관 정보 설정
    }
}

