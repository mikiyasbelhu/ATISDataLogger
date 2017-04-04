package com.example.mike.atisgpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

import java.util.HashMap;
import java.util.Map;


/**
 * Created by mike on 4/3/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private DBHandler db;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        db = new DBHandler(context, null, null, 1);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //getting all the unsynced names
                Cursor cursor = db.getUnsyncedLogs();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced gps to MySQL
                        saveGPSLog(
                                cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_ID)),
                                cursor.getDouble(cursor.getColumnIndex(DBHandler.COLUMN_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(DBHandler.COLUMN_LONGITUDE)),
                                cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_TIME)),
                                cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_DIRECTION)),
                                cursor.getString(cursor.getColumnIndex(DBHandler.COLUMN_PHASE)),
                                cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_PROVIDER_ID)),
                                cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_ROUTE_ID)),
                                cursor.getInt(cursor.getColumnIndex(DBHandler.COLUMN_TRIP_ID))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    /*
    * method taking two arguments
    * name that is to be saved and id of the name from SQLite
    * if the name is successfully sent
    * we will update the status as Mained in SQLite
    * */
    private void saveGPSLog(final Integer id, final Double latitude, final Double longitude, final String time, final int direction,
                            final String phase, final int provider_id, final int route_id, final int trip_id)

    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                db.updateGPSLogStatus(id, MainActivity.GPSLog_SYNCED_WITH_SERVER);

                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(MainActivity.DATA_SAVED_BROADCAST));
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

                        } else if (error instanceof VolleyError) {

                        } else if (error instanceof NetworkError) {

                        } else if (error instanceof NoConnectionError) {

                        } else if (error instanceof ParseError) {

                        }
                    }
                }) {
            @Override
            protected Map getParams() throws AuthFailureError {
                Map params = new HashMap<>();
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                params.put("time", String.valueOf(time));
                params.put("direction", String.valueOf(direction));
                params.put("phase", String.valueOf(phase));
                params.put("provider_id", String.valueOf(provider_id));
                params.put("route_id", String.valueOf(route_id));
                params.put("trip_id", String.valueOf(trip_id));
                return params;
            }
        };

        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

}