package me.samlss.inkjet.managers;

import android.annotation.SuppressLint;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import me.samlss.framework.log.PLog;
import me.samlss.framework.permission.AppPermissionConstant;
import me.samlss.framework.permission.AppPermissionUtil;
import me.samlss.framework.utils.AppUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 位置管理器
 */
public class LocationManager {
    private static LocationManager sInstance;
    private AMapLocationClientOption mAMapLocationClientOption;

    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient;
    private AMapLocation mCurrentLocation;

    private LocationManager(){
        mLocationClient = new AMapLocationClient(AppUtils.getApp());
        mLocationClient.setLocationListener(mLocationListener);

        mAMapLocationClientOption = new AMapLocationClientOption();
        mAMapLocationClientOption.setInterval(1000 * 60); //省电，一分钟刷新一次
//        mAMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
    }

    public static LocationManager get() {
        if (sInstance == null){
            synchronized (LocationManager.class){
                if (sInstance == null){
                    sInstance = new LocationManager();
                }
            }
        }
        return sInstance;
    }

    public static AMapLocation getCurrentLocation() {
        return sInstance.mCurrentLocation;
    }

    /**
     * 开始位置监听
     * */
    @SuppressLint("MissingPermission")
    public void startListening(){
        if (!AppPermissionUtil.hasPermission(AppPermissionConstant.LOCATION_GROUP)){
            return;
        }

        mLocationClient.setLocationOption(mAMapLocationClientOption);
        mLocationClient.startLocation();
    }

    public void stopListening(){
        mLocationClient.stopLocation();
    }

    public void destroy(){
        mLocationClient.unRegisterLocationListener(mLocationListener);
        mLocationClient.onDestroy();
    }

    /**
     * 获取经纬度,纬度在前
     * */
    public static String getGPSLocation(){
        if (sInstance.mCurrentLocation == null){
            return "(0,0)";
        }
        return "(" + sInstance.mCurrentLocation.getLatitude() + ", " + sInstance.mCurrentLocation.getLongitude() + ")";
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            mCurrentLocation = aMapLocation;
            if (aMapLocation != null) {
                PLog.e("address: "+aMapLocation.getAddress());
                PLog.e("location: " + "(" + aMapLocation.getLatitude() + "," + aMapLocation.getLongitude() + ")");
            }
        }
    };
}
