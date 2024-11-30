package com.example.chairman_server.dto.rental;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRentalResponse {
    private Long rentalId;
    private String rentalCode;
    private String status;
    private String rentalDate;
    private String returnDate;
    private String userName; // user.name 값
    private String wheelchairType; // wheelchair.type 값
    private Long wheelchairId; // wheelchair.wheelchairId 값
    private String institutionName; // institution.name 값
    private String institutionAddress; // institution.address 값
    private String institutionPhone; // institution.telNumber 값
    private Long institutionCode; // institution.institutionCode 값
}

