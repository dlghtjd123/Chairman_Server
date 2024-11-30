package com.example.chairman_server.dto.rental;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRentalResponse {
    private Long rentalId;
    private String rentalCode;
    private String status;
    private String rentalDate;
    private String returnDate;
    private String userPhoneNumber;
    private String wheelchairType; // Wheelchair type 값
    private String institutionName; // Institution name 값
    private String institutionAddress; // Institution address 값
    private String institutionPhone; // Institution telNumber 값
    private Long institutionCode; // Institution code 값
}
