package com.example.busmate;

public class BusData {
    private String arrival_time;
    private String bus_number_plate;
    private String departure_time;
    private String route_name;
    private String route_number;

    public BusData() {
        // Default constructor required for calls to DataSnapshot.getValue(BusData.class)
    }

    public BusData(String arrival_time, String bus_number_plate, String departure_time, String route_name, String route_number) {
        this.arrival_time = arrival_time;
        this.bus_number_plate = bus_number_plate;
        this.departure_time = departure_time;
        this.route_name = route_name;
        this.route_number = route_number;
    }

    // Getters and Setters
    public String getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    public String getBus_number_plate() {
        return bus_number_plate;
    }

    public void setBus_number_plate(String bus_number_plate) {
        this.bus_number_plate = bus_number_plate;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public String getRoute_number() {
        return route_number;
    }

    public void setRoute_number(String route_number) {
        this.route_number = route_number;
    }
}
