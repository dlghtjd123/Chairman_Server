package com.example.chairman_server.domain.rental;

public enum RentalStatus {
    ACTIVE, RETURNED, CANCELLED, WAITING, NORMAL
    //ACTIVE : 현재 대여중
    //RETURNED : 반납 완료
    //CANCELLED : 대여 취소
    //WAITING : 대여 요청 대기
    //NORMAL : 기본 디폴트 상태
}
