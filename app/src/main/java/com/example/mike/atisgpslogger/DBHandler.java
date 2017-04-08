package com.example.mike.atisgpslogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mike on 3/30/2017.
 */

public class DBHandler extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "gpslog.db";

    public static final String TABLE_DRIVER = "driver";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";

    public static final String TABLE_GPSLog = "gpslog";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DIRECTION = "direction";
    public static final String COLUMN_PHASE = "phase";
    public static final String COLUMN_PROVIDER_ID = "provider_id";
    public static final String COLUMN_ROUTE_ID = "route_id";
    public static final String COLUMN_TRIP_ID = "trip_id";
    public static String COLUMN_STATUS = "status";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_GPSLog + " ( " + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_VEHICLE_ID + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_DIRECTION + " INTEGER, " +
                COLUMN_PHASE + " INTEGER, " +
                COLUMN_PROVIDER_ID + " INTEGER, " +
                COLUMN_ROUTE_ID + " INTEGER, " +
                COLUMN_TRIP_ID + " TEXT, " +
                COLUMN_STATUS + " INTEGER " +
                ");";
        sqLiteDatabase.execSQL(query);

        String query2 = "CREATE TABLE " + TABLE_DRIVER + " ( " + COLUMN_VEHICLE_ID +
                " TEXT PRIMARY KEY , " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PHONE + " INTEGER " +
                ");";
        sqLiteDatabase.execSQL(query2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GPSLog);
        onCreate(sqLiteDatabase);
    }

    public void addLog(GPSLog gPSLog) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_ID, gPSLog.getVehicle_id());
        values.put(COLUMN_LATITUDE, gPSLog.getLatitude());
        values.put(COLUMN_LONGITUDE, gPSLog.getLongitude());
        values.put(COLUMN_TIME, gPSLog.getTime());
        values.put(COLUMN_DIRECTION, gPSLog.getDirection());
        values.put(COLUMN_PHASE, gPSLog.getPhase());
        values.put(COLUMN_PROVIDER_ID, gPSLog.getProvider_id());
        values.put(COLUMN_ROUTE_ID, gPSLog.getRoute_id());
        values.put(COLUMN_TRIP_ID, gPSLog.getTrip_id());
        values.put(COLUMN_STATUS, gPSLog.getStatus());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.insert(TABLE_GPSLog, null, values);
        sqLiteDatabase.close();
    }

    public void addDriver(Driver driver) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_ID, driver.getVehicle_id());
        values.put(COLUMN_NAME, driver.getName());
        values.put(COLUMN_PHONE, driver.getPhone());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.insert(TABLE_DRIVER, null, values);
        sqLiteDatabase.close();
    }

    public void deleteLog(String latitude) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_GPSLog + " WHERE " + COLUMN_LATITUDE + " = \"" + latitude + "\";");
    }

    /*
    * this method will give us all the name stored in sqlite
    * */
    public Cursor getLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_GPSLog + " ORDER BY " + COLUMN_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    /*
    * this method is for getting all the unsynced name
    * so that we can sync it with database
    * */
    public Cursor getUnsyncedLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_GPSLog + " WHERE " + COLUMN_STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    /*
    * This method taking two arguments
    * first one is the id of the name for which
    * we have to update the sync status
    * and the second one is the status that will be changed
    * */

    public boolean updateGPSLogStatus(int id, int status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        Integer x = db.update(TABLE_GPSLog, contentValues, COLUMN_ID + " = " + id, null);
        db.close();
        return true;
    }

}
