package com.example.mike.atisgpslogger;

/**
 * Created by mike on 4/6/2017.
 */

public class Driver {

    private String vehicle_id;
    private String name;
    private int phone;

    public Driver(String vehicle_id, String name, int phone) {
        this.vehicle_id = vehicle_id;
        this.name = name;
        this.phone = phone;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
