package com.example.busmate;

public class Rating {
    private String id;
    private String name;
    private String email;
    private String busNumber;
    private String category;
    private String userType;
    private float ratingValue;
    private String comment;
    private String date;

    // Required empty constructor for Firebase
    public Rating() {
    }

    public Rating(String id, String name, String email, String busNumber, String category,
                  String userType, float ratingValue, String comment, String date) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.busNumber = busNumber;
        this.category = category;
        this.userType = userType;
        this.ratingValue = ratingValue;
        this.comment = comment;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public float getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(float ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}