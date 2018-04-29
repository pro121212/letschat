package com.xinsane.util;

import android.util.Log;

public class LogUtil {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static int level = DEBUG;

    public static void v(String msg) {
        if (level > VERBOSE)
            return;
        Log.v("LogUtil", msg);
    }

    public static void v(String msg, String tag) {
        if (level > VERBOSE)
            return;
        Log.v("LogUtil/" + tag, msg);
    }

    public static void d(String msg) {
        if (level > DEBUG)
            return;
        Log.d("LogUtil", msg);
    }

    public static void d(String msg, String tag) {
        if (level > DEBUG)
            return;
        Log.d("LogUtil/" + tag, msg);
    }

    public static void i(String msg) {
        if (level > INFO)
            return;
        Log.i("LogUtil", msg);
    }

    public static void i(String msg, String tag) {
        if (level > INFO)
            return;
        Log.i("LogUtil/" + tag, msg);
    }

    public static void w(String msg) {
        if (level > WARN)
            return;
        Log.w("LogUtil", msg);
    }

    public static void w(String msg, String tag) {
        if (level > WARN)
            return;
        Log.w("LogUtil/" + tag, msg);
    }

    public static void e(String msg) {
        if (level > ERROR)
            return;
        Log.e("LogUtil", msg);
    }

    public static void e(String msg, String tag) {
        if (level > ERROR)
            return;
        Log.e("LogUtil/" + tag, msg);
    }
}
