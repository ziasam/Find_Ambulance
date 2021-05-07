package com.brainfluence.pickmeuprebuild.model;

public class User {
    public String name,email,phoneNumber,password,token,profilePicture,userType,userId;

    public User() {
        this.userId = null;
        this.name = null;
        this.email = null;
        this.phoneNumber = null;
        this.password = null;
        this.token = null;
        this.profilePicture = null;
        this.userType = null;
    }

    public User(String name, String email, String phoneNumber, String password, String token, String profilePicture, String userType, String userId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.token = token;
        this.profilePicture = profilePicture;
        this.userType = userType;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
