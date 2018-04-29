package com.xinsane.letschat.util;

import android.annotation.SuppressLint;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xinsane.letschat.App;
import com.xinsane.util.LogUtil;

public class Location {

    @SuppressLint("StaticFieldLeak")
    private static AMapLocationClient mLocationClient =
            new AMapLocationClient(App.getContext());

    private static String province, city;
    private static Listener listener;

    static {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        option.setOnceLocation(true);
        option.setOnceLocationLatest(true);
        option.setLocationCacheEnable(false);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {
                    province = aMapLocation.getProvince().replace("省", "");
                    city = province + aMapLocation.getCity().replace("市", "");
                    if (listener != null)
                        listener.onLocation(city);
                } else {
                    LogUtil.e("无法获取定位：code=" + aMapLocation.getErrorCode() +
                                ", msg=" + aMapLocation.getErrorInfo());
                }
            }
        });
    }

    public static void start() {
        mLocationClient.startLocation();
    }

    public static void setListener(Listener listener) {
        Location.listener = listener;
    }

    public static String getCity() {
        return city;
    }

    public interface Listener {
        void onLocation(String city);
    }
}
