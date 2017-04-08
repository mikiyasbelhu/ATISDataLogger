package com.example.mike.atisgpslogger;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 4/4/2017.
 */

public class LocationService extends Service implements LocationListener {

    public static final String URL_SAVE_NAME = "http://atis.000webhostapp.com/saveGPS.php";

    //database helper object
    private DBHandler db;

    //private GPSAdapter gpsAdapter;
    ATISGPSLogger atisgpsLogger;

    //Broadcast receiver for exit
    private BroadcastReceiver exitBroadcastReceiver;


    //1 means data is synced and 0 means data is not synced
    public static final int GPSLog_SYNCED_WITH_SERVER = 1;
    public static final int GPSLog_NOT_SYNCED_WITH_SERVER = 0;
    public static final double BUS_STATIONS_RADIUS = 30;

    public static final String EXIT_BROADCAST = "com.example.mike.atisgpslogger.exit";
    LocationManager mLocationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        atisgpsLogger = (ATISGPSLogger) getApplication();

        atisgpsLogger.notificationIntent = new Intent(this, MainActivity.class);

        atisgpsLogger.pendingIntent = PendingIntent.getActivity(this, 0, atisgpsLogger.notificationIntent, 0);

        atisgpsLogger.notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("ATIS working")
                .setContentTitle("Logging gps data ...")
                .setContentIntent(atisgpsLogger.pendingIntent).build();

        startForeground(1337, atisgpsLogger.notification);

        //the broadcast receiver for new data
        exitBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopForeground(true);
                System.exit(0);
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(exitBroadcastReceiver, new IntentFilter(EXIT_BROADCAST));

        db = new DBHandler(this);

        SharedPreferences sharedPref = getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        Integer update_frequency = 1000 * (Integer.valueOf(sharedPref.getString("update_frequency", "")));

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, update_frequency, 0, this);

    }

    /*
    * this method is saving the log to the server
    * */
    public void saveGPSLogToServer(final Location location) {

        SharedPreferences sharedPref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        final Integer provider_id = Integer.valueOf(sharedPref.getString("bus_provider", ""));
        final Integer route_id = Integer.valueOf(sharedPref.getString("bus_routes", ""));
        final String vehicle_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        Date currentDate = new Date(location.getTime());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String time = dateFormat.format(currentDate);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                saveNameToLocalStorage(location, GPSLog_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                saveNameToLocalStorage(location, GPSLog_NOT_SYNCED_WITH_SERVER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError) {

                        } else if (error instanceof AuthFailureError) {

                        } else if (error instanceof VolleyError) {

                        } else if (error instanceof NetworkError) {

                        } else if (error instanceof NoConnectionError) {

                        } else if (error instanceof ParseError) {

                        }
                        //on error storing the name to sqlite with status unsynced
                        saveNameToLocalStorage(location, GPSLog_NOT_SYNCED_WITH_SERVER);
                    }
                }) {
            @Override
            protected Map getParams() throws AuthFailureError {
                Map params = new HashMap<>();
                params.put("vehicle_id", String.valueOf(vehicle_id));
                params.put("latitude", String.valueOf(location.getLatitude()));
                params.put("longitude", String.valueOf(location.getLongitude()));
                params.put("time", String.valueOf(time));
                params.put("direction", String.valueOf(1));
                params.put("phase", String.valueOf(1));
                params.put("provider_id", String.valueOf(provider_id));
                params.put("route_id", String.valueOf(route_id));
                params.put("trip_id", String.valueOf(1));
                return params;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //saving the name to local storage
    void saveNameToLocalStorage(Location location, int status) {
        SharedPreferences sharedPref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        final Integer provider_id = Integer.valueOf(sharedPref.getString("bus_provider", ""));
        final Integer route_id = Integer.valueOf(sharedPref.getString("bus_routes", ""));
        final String vehicle_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        GPSLog gpslog = new GPSLog(location.getLatitude(), location.getLongitude());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String time;
        Date currentDate = new Date(location.getTime());
        time = dateFormat.format(currentDate);
        gpslog.setTime(time);
        if (true) {
//            if (atisgpsLogger.getCloseStation() == null) {
//                atisgpsLogger.setCloseStation(CloseBusStation(gpslog));
//            } else if (atisgpsLogger.getCurStation() == null) {
//                int direction = SetStationInfo(atisgpsLogger.getCloseStation(), gpslog);
//                atisgpsLogger.setTrip_direction(direction);
//            } else {
//                GPSLog gpslog1 = UpdateStationInfo(gpslog);
//
//                gpslog1.setProvider_id(provider_id);
//                gpslog1.setRoute_id(route_id);
//                gpslog1.setTrip_id(atisgpsLogger.getTrip_Id());
//                gpslog1.setStatus(status);
            db.addLog(gpslog);
            atisgpsLogger.gpsLogs.add(gpslog);
            atisgpsLogger.refreshList();
            //  }

        } else {
            // TODO kill the app and stop listening to GPS updates
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location changed", Toast.LENGTH_SHORT).show();
        saveNameToLocalStorage(location, GPSLog_NOT_SYNCED_WITH_SERVER);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private BusStation CloseBusStation(GPSLog curLog) {

        /*
        * array that holds the neighbor's of the stations that is closer to current positions
        * */
        BusStation[] neighboring;
        double shortDistance;
        int index;


        /*
        * calculate every distance form current positions to all stations
        * */
        for (int i = 0; i < ATISGPSLogger.sStations.length; i++) {
            GPSLog busLog = new GPSLog(ATISGPSLogger.sStations[i].getLatitude(),
                    ATISGPSLogger.sStations[i].getLatitude());

            ATISGPSLogger.sStations[i].setDistance(ATISGPSLogger.dis(busLog, curLog));
        }

        shortDistance = ATISGPSLogger.sStations[0].getDistance();
        index = 0;

        /*
        * identify the closest station
        * */

        for (int j = 0; j < ATISGPSLogger.sStations.length; j++) {
            if (shortDistance > ATISGPSLogger.sStations[j].getDistance()) {
                shortDistance = ATISGPSLogger.sStations[j].getDistance();
                index = j;
            }
        }
        return ATISGPSLogger.sStations[index];
    }

    private int SetStationInfo(BusStation closeStation, GPSLog newLog) {
        /*
        * convention
        * ShiroMeda => Le'gahar  0
        *
        * Le'gahar => Shiromeda  1
        *
        * */


        // the station that is assumed to be next to the closest
        // station.
        BusStation FarStation = closeStation.getNeighboringStations()[0];

        //the stations that is next to the closest station if the
        //the above assumption is wrong.
        BusStation ERRStation = closeStation.getNeighboringStations()[1];

        //convert the BusStation data type to GPSLog
        GPSLog closeSt = new GPSLog(closeStation.getLatitude(), closeStation.getLongitude());
        GPSLog FarSt = new GPSLog(FarStation.getLatitude(), FarStation.getLongitude());

        //calculate new distance
        double newCloseStationDistance = ATISGPSLogger.dis(newLog, closeSt);
        double newFarStationDistance = ATISGPSLogger.dis(newLog, FarSt);

        /*
        *
        * when the new location is closer to the closest station.
        * the bus is heading towards the closest station
        *
        * */
        if (newCloseStationDistance < closeStation.getDistance()
                && newFarStationDistance > FarStation.getDistance()) {
            atisgpsLogger.setNextStation(closeStation);
            atisgpsLogger.setCurStation(FarStation);
            return 1;
        }

         /*
        *
        * when the new location is closer to the the station,that is next to the closest station.
        * the bus is heading away form the closest station
        *
        * */
        else if (newCloseStationDistance > closeStation.getDistance()
                && newFarStationDistance < FarStation.getDistance()) {
            atisgpsLogger.setNextStation(FarStation);
            atisgpsLogger.setCurStation(closeStation);
            return 0;
        }

        /*
        *
        * when the new location is closer to both  the station,that is next to the closest station,
        * and to the closest station.
        * the bus is heading toward the closest station
        *
        * */
        else if (newCloseStationDistance < closeStation.getDistance()
                && newFarStationDistance < FarStation.getDistance()) {
            atisgpsLogger.setNextStation(closeStation);
            atisgpsLogger.setCurStation(ERRStation);
            return 0;
        }
       /*
        *
        * when the new location is closer to neither the station,that is next to the closest station,
        * nor to the closest station.
        * the bus is heading away from the closest station
        *
        * */
        else if (newCloseStationDistance > closeStation.getDistance()
                && newFarStationDistance > FarStation.getDistance()) {
            atisgpsLogger.setNextStation(ERRStation);
            atisgpsLogger.setCurStation(closeStation);
            return 1;
        }
        /*
        * Uncaught error
        * */
        else
            return -1;

    }

    private GPSLog UpdateStationInfo(GPSLog log) {
        GPSLog nxtBusStation = new GPSLog(atisgpsLogger.getNextStation().getLatitude(),
                atisgpsLogger.getNextStation().getLongitude());
        double newDis = atisgpsLogger.dis(nxtBusStation, log);
        if (newDis <= BUS_STATIONS_RADIUS && !atisgpsLogger.getNextStation().isTerminal()) {
            atisgpsLogger.setCloseStation(atisgpsLogger.getNextStation());
            atisgpsLogger.setNextStation(atisgpsLogger.getNextStation()
                    .getNeighboringStations()[atisgpsLogger.getTrip_direction()]);
            atisgpsLogger.setCurStation(atisgpsLogger.getCloseStation());

            if (atisgpsLogger.isNew_trip()) {

                if (atisgpsLogger.getTrip_direction() == 0)
                    atisgpsLogger.setTrip_direction(1);
                else
                    atisgpsLogger.setTrip_direction(0);
                atisgpsLogger.setTrip_Id(log.getTime() + atisgpsLogger.getTrip_direction() + "");
                atisgpsLogger.setNew_trip(false);
            }

            if (atisgpsLogger.getNextStation().isTerminal()) {
                atisgpsLogger.setNew_trip(true);
                log.setDirection(atisgpsLogger.getTrip_direction());
                log.setPhase(0);
            } else {
                log.setDirection(atisgpsLogger.getTrip_direction());
                log.setPhase(1);
            }
        }

        return log;
    }
}
