package com.example.chairman_server.domain.wheelchair;

public enum WheelchairStatus {
    AVAILABLE,RENTED,WAITING,BROKEN,ACCEPTED
    //AVAILABLE : 대여 가능
    //RENTED : 대여 중
    //WAITING : 대여 요청 대기 중
    //BROKEN : 파손
    //ACCEPTED : 대여 요청 수락 후 대여까지의 기간
}
