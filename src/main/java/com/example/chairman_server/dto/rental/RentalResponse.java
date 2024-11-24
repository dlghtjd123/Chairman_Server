package com.example.chairman_server.dto.rental;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {
    private Long RentalId;
    private String WheelchairType;
    private String rentalDate;      // 대여 날짜
    private String returnDate;      // 반납 날짜
    private String status;          // 대여 상태 추가
    private String institutionName; // 공공기관 이름 추가
    private String institutionAddress; // 공공기관 주소 추가
    private String institutionPhone;   // 공공기관 전화번호 추가
    private Long institutionCode; // 공공기관 코드 추가
}
