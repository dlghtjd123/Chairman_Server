package com.example.chairman_server.domain.rental;

public enum RentalStatus {
    ACTIVE, WAITING, NORMAL, ACCEPTED
    //ACTIVE : 현재 대여중
    //WAITING : 대여 요청 대기
    //NORMAL : 기본 디폴트 상태
    //ACCEPTED : 대기 요청 수락
}
