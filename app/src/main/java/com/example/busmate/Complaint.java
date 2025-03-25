package com.example.busmate;

public class Complaint {
    private String id;
    private String userName;
    private String email;
    private String busNumber;
    private String complaintType;
    private String location;
    private String userType; // Passenger or Conductor
    private String description;
    private String dateSubmitted;
    private String status;

    // Empty constructor needed for Firebase
    public Complaint() {
    }

    public Complaint(String id, String userName, String email, String busNumber,
                     String complaintType, String location, String userType,
                     String description, String dateSubmitted, String status) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.busNumber = busNumber;
        this.complaintType = complaintType;
        this.location = location;
        this.userType = userType;
        this.description = description;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(String dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}