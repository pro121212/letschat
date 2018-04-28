package com.xinsane.letschat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.xinsane.letschat.util.Location;

@SuppressLint("StaticFieldLeak")
public class App extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Location.start();
    }

}
