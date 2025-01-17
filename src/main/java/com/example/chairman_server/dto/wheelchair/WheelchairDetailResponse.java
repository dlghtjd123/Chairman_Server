package com.example.chairman_server.dto.wheelchair;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WheelchairDetailResponse {
    private Long wheelchairId;       // 휠체어 ID
    private String type;             // 휠체어 타입
    private String wheelchairStatus; // 휠체어 상태
    private String rentalStatus;     // 대여 상태
    private String userName;         // 대여자 이름
    private String userPhone;        // 대여자 전화번호
}
