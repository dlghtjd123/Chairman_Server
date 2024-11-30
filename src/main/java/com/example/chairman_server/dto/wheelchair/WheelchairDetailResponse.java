package com.example.chairman_server.dto.wheelchair;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WheelchairDetailResponse {
    private Long wheelchairId;
    private String type; // 휠체어 타입
    private String status; // 휠체어 상태
    private String userName; // 대여자 이름 (대여 중인 경우)
    private String userPhone; // 대여자 전화번호 (대여 중인 경우)
}
