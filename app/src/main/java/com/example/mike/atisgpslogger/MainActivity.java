package com.example.mike.atisgpslogger;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    public static final String URL_SAVE_NAME = "http://192.168.137.44/atis_server/saveGPS.php";

    //database helper object
    private DBHandler db;

    //View objects
    private ListView listViewGps;

    //List to store all the names
    private List<GPSLog> gpsLogs;

    //1 means data is synced and 0 means data is not synced
    public static final int GPSLog_SYNCED_WITH_SERVER = 1;
    public static final int GPSLog_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "com.example.mike.atisgpslogger.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private GPSAdapter gpsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean gps_enabled = false;
        boolean network_enabled = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHandler(this, null, null, 1);
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);

        SharedPreferences sharedPref = getSharedPreferences(this.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        Integer update_frequency = 1000 * (Integer.valueOf(sharedPref.getString("update_frequency", "")));

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        gpsLogs = new ArrayList<>();

        listViewGps = (ListView) findViewById(R.id.listViewLogs);

        //calling the method to load all the stored names
        loadLogs();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again
                loadLogs();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Unable to get your location.");
            dialog.setMessage("Do you want to turn on your gps?");
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });

            dialog.setNegativeButton("No", null);
            dialog.create().show();
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, update_frequency, 0, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

     /*
    * this method will
    * load the names from the database
    * with updated sync status
    * */

    private void loadLogs() {
        gpsLogs.clear();
        Cursor cursor = db.getLogs();
        if (cursor.moveToFirst()) {
            do {
                GPSLog gpsLog = new GPSLog(
                        cursor.getDouble(cursor.getColumnIndex(DBHandler.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(DBHandler.COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_DIRECTION)),
                        cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_PHASE)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_PROVIDER_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_ROUTE_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_TRIP_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_STATUS))
                );
                gpsLogs.add(gpsLog);
            } while (cursor.moveToNext());
        }

        gpsAdapter = new GPSAdapter(this, R.layout.logs, gpsLogs);
        listViewGps.setAdapter(gpsAdapter);
    }

    /*
    * this method will simply refresh the list
    * */
    private void refreshList() {
        gpsAdapter.notifyDataSetChanged();
    }

    /*
    * this method is saving the log to the server
    * */
    public void saveGPSLogToServer(final Location location) {

        SharedPreferences sharedPref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        final Integer provider_id = Integer.valueOf(sharedPref.getString("bus_provider", ""));
        final Integer route_id = Integer.valueOf(sharedPref.getString("bus_routes", ""));


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
                            Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof NoConnectionError) {
                            Toast.makeText(MainActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {

                        }
                        //on error storing the name to sqlite with status unsynced
                        saveNameToLocalStorage(location, GPSLog_NOT_SYNCED_WITH_SERVER);
                    }
                }) {
            @Override
            protected Map getParams() throws AuthFailureError {
                Map params = new HashMap<>();
                params.put("latitude", String.valueOf(location.getLatitude()));
                params.put("longitude", String.valueOf(location.getLongitude()));
                params.put("time", String.valueOf(time));
                params.put("direction", String.valueOf(1));
                params.put("phase", "LAYOVER DURING");
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


        Date currentDate = new Date(location.getTime());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String time = dateFormat.format(currentDate);

        GPSLog gpslog = new GPSLog(location.getLatitude(), location.getLongitude(), time, 1, "LAYOVER DURING", provider_id, route_id, 1, status);
        db.addLog(gpslog);
        gpsLogs.add(gpslog);
        refreshList();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.contact_us) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        saveGPSLogToServer(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please turn your GPS on...");
        progressDialog.show();
    }
}
