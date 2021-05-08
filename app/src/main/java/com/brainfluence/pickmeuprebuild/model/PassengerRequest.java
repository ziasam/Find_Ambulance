package com.brainfluence.pickmeuprebuild.model;

public class PassengerRequest {
    String userId,name,phoneNumber;
    Double pickupLat,pickupLon,hospitalLat,hospitalLon;

    public PassengerRequest() {
        this.userId = null;
        this.name = null;
        this.phoneNumber = null;
        this.pickupLat = null;
        this.pickupLon = null;
        this.hospitalLat = null;
        this.hospitalLon = null;
    }

    public PassengerRequest(String userId, String name, String phoneNumber, Double pickupLat, Double pickupLon, Double hospitalLat, Double hospitalLon) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.pickupLat = pickupLat;
        this.pickupLon = pickupLon;
        this.hospitalLat = hospitalLat;
        this.hospitalLon = hospitalLon;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLon() {
        return pickupLon;
    }

    public void setPickupLon(Double pickupLon) {
        this.pickupLon = pickupLon;
    }

    public Double getHospitalLat() {
        return hospitalLat;
    }

    public void setHospitalLat(Double hospitalLat) {
        this.hospitalLat = hospitalLat;
    }

    public Double getHospitalLon() {
        return hospitalLon;
    }

    public void setHospitalLon(Double hospitalLon) {
        this.hospitalLon = hospitalLon;
    }
}
