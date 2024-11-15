package com.example.chairman_server.dto.rental;

import com.example.chairman_server.domain.wheelchair.WheelchairType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RentalRequest {

    private Long institutionCode;

    private String email;

    private WheelchairType wheelchairType;

    private String rentalDate;

    private String returnDate; // ISO 형식의 날짜

}
