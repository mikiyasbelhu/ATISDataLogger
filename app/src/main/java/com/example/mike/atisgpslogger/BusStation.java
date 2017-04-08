package com.example.mike.atisgpslogger;

/**
 * Created by Menasi on 4/6/2017.
 */

public class BusStation {
    private String stationName;
    private double longitude;
    private double latitude;
    private double distance;
    private boolean isTerminal;
    private BusStation [] neighboringStations;

    public void setNeighboringStations(BusStation[] neighboringStations) {
        this.neighboringStations = neighboringStations;
    }

    public BusStation(String stationName, double longitude, double latitude, boolean isTerminal
                      ) {
        this.stationName = stationName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.isTerminal = isTerminal;

    }

    public String getStationName() {
        return stationName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public BusStation[] getNeighboringStations() {
        return neighboringStations;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
