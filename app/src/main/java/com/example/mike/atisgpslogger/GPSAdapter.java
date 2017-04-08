package com.example.mike.atisgpslogger;

/**
 * Created by mike on 4/3/2017.
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Belal on 1/27/2017.
 */

public class GPSAdapter extends ArrayAdapter<GPSLog> {

    //storing all the GPSLogs in the list
    private List<GPSLog> gpsLogs;

    //context object
    private Context context;

    //constructor
    public GPSAdapter(Context context, int resource, List<GPSLog> gpsLogs) {
        super(context, resource, gpsLogs);
        this.context = context;
        this.gpsLogs = gpsLogs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GpsViewHolder gpsViewHolder = null;
        View listViewItem = convertView;

        if (listViewItem == null)
        {
            //getting the layoutinflater
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //getting listview items
            listViewItem = inflater.inflate(R.layout.logs, null);
            gpsViewHolder = new GpsViewHolder(listViewItem);
            listViewItem.setTag(gpsViewHolder);
        }
        else
        {
            gpsViewHolder = (GpsViewHolder) listViewItem.getTag();
        }

        //getting the current log
        GPSLog gpsLog = gpsLogs.get(getCount() - position -1);

        //setting the gpsLog to textview
        gpsViewHolder.textViewLog.setText(
                " Log ID: " + String.valueOf(getCount() - position -1)+"\n"
                + "Time: " + String.valueOf(gpsLog.getTime()) + "\n"
                + "Latitude: " + String.valueOf(gpsLog.getLatitude()) + "\n"
                + "Longitude: " + String.valueOf(gpsLog.getLongitude())+"\n");

        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (gpsLog.getStatus() == 0)
            gpsViewHolder.imageViewStatus.setBackgroundResource(R.drawable.ic_queued);
        else
            gpsViewHolder.imageViewStatus.setBackgroundResource(R.drawable.ic_cloud_done_green);

        return listViewItem;
    }

    @Nullable
    @Override
    public GPSLog getItem(int position) {
        return super.getItem(super.getCount() - position -1);
    }

    class GpsViewHolder {

        TextView textViewLog;
        ImageView imageViewStatus;

        GpsViewHolder (View listViewItem){

            textViewLog = (TextView) listViewItem.findViewById(R.id.textViewLog);
            imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);

        }
    }
}