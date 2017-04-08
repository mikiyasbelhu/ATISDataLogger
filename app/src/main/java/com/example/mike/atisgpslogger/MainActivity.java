package com.example.mike.atisgpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
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

import static com.example.mike.atisgpslogger.LocationService.EXIT_BROADCAST;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AlarmReceiver alarm = new AlarmReceiver();

    //database helper object
    private DBHandler db;

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //Broadcast receiver for new data
    private BroadcastReceiver newLogBroadcastReceiver;

    //broadcasts
    public static final String DATA_SAVED_BROADCAST = "com.example.mike.atisgpslogger.datasaved";
    public static final String NEW_DATA_BROADCAST = "com.example.mike.atisgpslogger.newdata";

    static String KEY_IS_FIRST_TIME = "com.example.mike.atisgpslogger.first_time";
    static String KEY = "com.example.mike.atisgpslogger";

    protected ATISGPSLogger aitsGPSLOGGER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        db = new DBHandler(this);

        super.onCreate(savedInstanceState);

        aitsGPSLOGGER = (ATISGPSLogger) getApplication();

        if (isFirstTime()) {
            Intent intent = new Intent(this, FirstTimeActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);

            PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);

            SharedPreferences sharedPref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
            Integer working_hours = Integer.valueOf(sharedPref.getString("working_hours", ""));

            alarm.setAlarm(this,working_hours);

            aitsGPSLOGGER.listViewGps = (ListView) findViewById(R.id.listViewLogs);

            Intent i = new Intent(getBaseContext(), LocationService.class);

            getBaseContext().startService(i);

            aitsGPSLOGGER.loadLogs();

            //the broadcast receiver to update sync status
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //loading the logs again
                    aitsGPSLOGGER.loadLogs();
                }
            };

            //registering the broadcast receiver to update sync status
            registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));

            //the broadcast receiver for new data
            newLogBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //loading the logs again
                    aitsGPSLOGGER.loadLogs();
                }
            };

            //registering the broadcast receiver to update sync status
            registerReceiver(newLogBroadcastReceiver, new IntentFilter(NEW_DATA_BROADCAST));

            aitsGPSLOGGER.listViewGps.setAdapter(aitsGPSLOGGER.gpsAdapter);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!aitsGPSLOGGER.checkgps()) {
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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
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

        } else if (id == R.id.archive) {
            Uri webpage = Uri.parse("http://atis.000webhostapp.com/Database.php");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(webIntent);
        } else if (id == R.id.exit) {
            getApplicationContext().sendBroadcast(new Intent(EXIT_BROADCAST));
            stopService(new Intent(MainActivity.this, LocationService.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isFirstTime() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(KEY_IS_FIRST_TIME, true);
    }

}
