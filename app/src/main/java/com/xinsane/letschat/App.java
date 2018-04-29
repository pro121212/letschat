package com.xinsane.letschat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.xinsane.letschat.service.SocketService;
import com.xinsane.letschat.util.Location;

import org.litepal.LitePal;

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
        Fresco.initialize(this);
        LitePal.initialize(this);
        startService(new Intent(context, SocketService.class));
    }

}
