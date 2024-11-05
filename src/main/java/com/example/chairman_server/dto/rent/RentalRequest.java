package com.example.chairman_server.dto.rent;

import com.example.chairman_server.domain.wheelchair.WheelchairType;


public class RentalRequest {
    private WheelchairType wheelchairType;
    private String returnDate; // ISO 형식의 날짜

    // Getters and Setters
    public WheelchairType getWheelchairType() {
        return wheelchairType;
    }

    public void setWheelchairType(WheelchairType wheelchairType) {
        this.wheelchairType = wheelchairType;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }
}
