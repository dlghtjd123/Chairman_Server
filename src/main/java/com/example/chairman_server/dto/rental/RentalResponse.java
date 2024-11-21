package com.example.chairman_server.dto.rental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {
    private String rentalCode;    // 대여 코드
    private String rentalDate;    // 대여 날짜
    private String returnDate;    // 반납 날짜
    private String status;        // 대여 상태
}
