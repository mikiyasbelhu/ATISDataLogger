package com.example.mike.atisgpslogger;

/**
 * Created by mike on 4/2/2017.
 */

public class GPSLog {

    private int _id;
    private double latitude;
    private double longitude;
    private String time;
    private int direction;
    private String phase;
    private int provider_id;
    private int route_id;
    private int trip_id;
    private int status;


    public GPSLog(Double latitude, Double longitude, String time, int direction, String phase, int provider_id, int route_id, int trip_id, int status) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.direction = direction;
        this.phase = phase;
        this.provider_id = provider_id;
        this.route_id = route_id;
        this.trip_id = trip_id;
        this.status = status;

    }


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(int provider_id) {
        this.provider_id = provider_id;
    }

    public int getRoute_id() {
        return route_id;
    }

    public void setRoute_id(int route_id) {
        this.route_id = route_id;
    }

    public int getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(int trip_id) {
        this.trip_id = trip_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
