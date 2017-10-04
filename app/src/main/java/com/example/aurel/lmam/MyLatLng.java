package com.example.aurel.lmam;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aurel on 14-12-16.
 */

public class MyLatLng
{
    private double lat;
    private double lng;

    private final String TAG = "ClassLatLng";

    public MyLatLng() {}

    public MyLatLng(double lat, double lng) {
        this.setLat(lat);
        this.lng = lng;
    }
    public MyLatLng(LatLng ActLatLng)
    {
        this.setLat(ActLatLng.latitude);
        this.lng = ActLatLng.longitude;
    }
    public LatLng  GetLatlng()
    {
        return new LatLng(this.lat, this.lng);
    }


    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getDistanceInMeters(MyLatLng DistanceWith)
    {
        // Note : https://fr.wikipedia.org/wiki/Orthodromie
        double Lat1Rad = Math.toRadians(this.getLat());
        double Lat2Rad = Math.toRadians(DistanceWith.getLat());
        double Long1Rad = Math.toRadians(this.getLng());
        double Long2Rad = Math.toRadians(DistanceWith.getLng());


        return  ((60*1852) * Math.toDegrees(Math.acos(Math.sin(Lat1Rad) * Math.sin(Lat2Rad)
                + Math.cos(Lat1Rad) * Math.cos(Lat2Rad)
                * Math.cos(Long2Rad - Long1Rad))));
    }
}
