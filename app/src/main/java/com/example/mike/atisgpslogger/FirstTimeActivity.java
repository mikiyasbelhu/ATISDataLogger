package com.example.mike.atisgpslogger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


public class FirstTimeActivity extends AppCompatActivity {

    EditText driverName;
    EditText phoneNumber;

    //database helper object
    private DBHandler db;

    static String KEY_IS_FIRST_TIME = "com.example.mike.atisgpslogger.first_time";
    static String KEY = "com.example.mike.atisgpslogger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);
        db = new DBHandler(this);
        driverName = (EditText) findViewById(R.id.driverName);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);

    }


    public void registerClicked(View view) {

        String vehicle_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String name = driverName.getText().toString();
        int phone = Integer.parseInt(phoneNumber.getText().toString());

        Driver driver = new Driver(vehicle_id,name,phone) ;
        db.addDriver(driver);

        getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_FIRST_TIME,false).commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

}
