package com.xinsane.letschat.util;

import android.annotation.SuppressLint;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.xinsane.letschat.App;

public class Location {

    @SuppressLint("StaticFieldLeak")
    private static AMapLocationClient mLocationClient =
            new AMapLocationClient(App.getContext());

    private static String country, city;

    static {
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                country = aMapLocation.getCountry();
                city = aMapLocation.getCity();
                mLocationClient.stopLocation();
            }
        });
    }

    public static void start() {
        mLocationClient.startLocation();
    }

    public static void stop() {
        mLocationClient.stopLocation();
    }

    public static String getCountry() {
        return country;
    }

    public static String getCity() {
        return city;
    }
}
