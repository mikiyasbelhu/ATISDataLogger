package com.example.mike.atisgpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Boot received", Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
            Integer working_hours = Integer.valueOf(sharedPref.getString("working_hours", ""));

            alarm.setAlarm(context,working_hours);
        }
    }
}
