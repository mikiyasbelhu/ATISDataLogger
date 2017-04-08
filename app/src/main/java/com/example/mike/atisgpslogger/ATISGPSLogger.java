package com.example.mike.atisgpslogger;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 4/5/2017.
 */

public class ATISGPSLogger extends Application {

    private static LocationManager mLocationManager;

    //a busStation object for the next Station
    private BusStation nextStation;

    //a busStation object for the current Station
    private BusStation curStation;

    //a busStation object for the current Station
    private BusStation closeStation;

    //the direction in which the bus is traveling
    private int trip_direction;

    //the unique identification of a trip
    private String trip_Id;

    private boolean new_trip;


    //adapter object for list view
    public GPSAdapter gpsAdapter;

    //storing all the GPSLogs in the list
    public List<GPSLog> gpsLogs;

    //View objects
    public ListView listViewGps;

    DBHandler db;

    public Intent notificationIntent;

    public PendingIntent pendingIntent;

    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();

        db = new DBHandler(this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        gpsLogs = new ArrayList<>();

        gpsAdapter = new GPSAdapter(getBaseContext(), R.layout.logs, gpsLogs);

        addToArray();

    }

    public static boolean checkgps() {
        boolean gps_enabled = false;
        gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gps_enabled;
    }

    /*
    * this method will simply refresh the list
    * */
    public void refreshList() {
        gpsAdapter.notifyDataSetChanged();
        getApplicationContext().sendBroadcast(new Intent(MainActivity.NEW_DATA_BROADCAST));
    }

    /*
    * this method will
    * load the names from the database
    * with updated sync status
    * */

    public void loadLogs() {
        gpsLogs.clear();
        Cursor cursor = db.getLogs();
        if (cursor.moveToFirst()) {
            do {
                GPSLog gpsLog = new GPSLog(
                        cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_VEHICLE_ID)),
                        cursor.getDouble(cursor.getColumnIndex(DBHandler.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(DBHandler.COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_DIRECTION)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_PHASE)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_PROVIDER_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_ROUTE_ID)),
                        cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_TRIP_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_STATUS))
                );
                gpsLogs.add(gpsLog);
            } while (cursor.moveToNext());
        }
        gpsAdapter = new GPSAdapter(this, R.layout.logs, gpsLogs);
        listViewGps.setAdapter(gpsAdapter);
    }

    public static double dis(GPSLog st, GPSLog dis) {

        double lat1 = st.getLatitude(), lon1 = st.getLongitude();
        double lat2 = dis.getLatitude(), lon2 = dis.getLongitude();
        double radius = 6371000; //m

        double dlat = Math.toRadians(lat2 - lat1);
        double dlng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dlng / 2) * Math.sin(dlng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = radius * c;

        return d;
    }

    static BusStation[] sStations = new BusStation[30];

    static private BusStation shiroMeda = new BusStation("Shiro Meda", 9.061790, 38.761350, true);

    private void addToArray() {
        sStations[0] = shiroMeda;
    }

    public BusStation getNextStation() {
        return nextStation;
    }

    public void setNextStation(BusStation nextStation) {
        this.nextStation = nextStation;
    }

    public BusStation getCurStation() {
        return curStation;
    }

    public void setCurStation(BusStation curStation) {
        this.curStation = curStation;
    }

    public BusStation getCloseStation() {
        return closeStation;
    }

    public void setCloseStation(BusStation closeStation) {
        this.closeStation = closeStation;
    }

    public int getTrip_direction() {
        return trip_direction;
    }

    public void setTrip_direction(int trip_direction) {
        this.trip_direction = trip_direction;
    }

    public String getTrip_Id() {
        return trip_Id;
    }

    public void setTrip_Id(String trip_Id) {
        this.trip_Id = trip_Id;
    }

    public boolean isNew_trip() {
        return new_trip;
    }

    public void setNew_trip(boolean new_trip) {
        this.new_trip = new_trip;
    }
}
