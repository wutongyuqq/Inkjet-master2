package me.samlss.inkjet.utils;

import android.content.Intent;
import android.net.Uri;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ActivityUtils;
import me.samlss.framework.utils.AppUtils;
import me.samlss.framework.utils.PackageUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 地图工具类
 */
public class MapUtils {
    private static String GAODE_MAP_PKG = "com.autonavi.minimap";
    private static String TENCENT_MAP_MAP_PKG = "com.tencent.map";
    private static String BAIDU_MAP_PKG = "com.baidu.BaiduMap";
    private static String GOOGLE_MAP_PKG = "com.google.android.apps.maps";

    private MapUtils(){
    }

    public static boolean hasGaodeMap(){
        return hasMap(GAODE_MAP_PKG);
    }

    public static boolean hasTencentMap(){
        return hasMap(TENCENT_MAP_MAP_PKG);
    }

    public static boolean hasBaiduMap(){
        return hasMap(BAIDU_MAP_PKG);
    }

    public static boolean hasGoogleMap(){
        return hasMap(GOOGLE_MAP_PKG);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openGaodeMap(String address, double latitude, double longitude){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage(GAODE_MAP_PKG);
        intent.setData(Uri.parse("androidamap://viewMap?sourceApplication=" + ResourceUtils.getString(R.string.app_name) +
                "&poiname=" + address +
                "&lat=" + latitude +
                "&lon=" + longitude +
                "&dev=0"));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openTencentMap(String address, double latitude, double longitude){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("qqmap://map/marker?marker=" +
                "coord:"+ latitude + "," + longitude +
                ";title:" + address +
                ";addr:" + address +
                "&referer=OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77"));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openBaiduMap(String address, double latitude, double longitude){
        Intent intent = new Intent();
        intent.setData(Uri.parse("baidumap://map/marker?" +
                "location="+latitude + "," + longitude +
                "&title=" + address +
                "&content=" + address +
                "&src=" + AppUtils.getApp().getPackageName()
        ));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openGoogleMap(String address, double latitude, double longitude){
        PLog.e("address: "+address);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(
                "geo:"+latitude + "," + longitude + "?q=" +address
        ));
        intent.setPackage(GOOGLE_MAP_PKG);
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openGaodeMapByBrowser(String address, double latitude, double longitude) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData((Uri.parse("https://uri.amap.com/marker?" +
                "position=" + latitude + "," + longitude +
                "&name=" + address +
                "&src=mypage&coordinate=gaode&callnative=0")));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openTencentMapByBrowser(String address, double latitude, double longitude) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData((Uri.parse("https://apis.map.qq.com/uri/v1/marker?" +
                "marker=coord:" + latitude + "," + longitude +";" +
                "title:" + address +
                ";addr:" + address)));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openBaiduMapByBrowser(String address, double latitude, double longitude){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://api.map.baidu.com/marker?" +
                "location="+latitude + "," + longitude +
                "&title=" + address +
                "&content=" + address +
                "&output=html" +
                "&src=" + AppUtils.getApp().getPackageName()
        ));
        ActivityUtils.startActivity(intent);
    }

    /**
     * 请自行捕获异常
     *
     * @param latitude 纬度
     * @param longitude 经度
     * */
    public static void openGoogleMapByBrowser(String address, double latitude, double longitude){
        StringBuffer stringBuffer = new StringBuffer("http://www.google.com/maps/search/?api=1&query=")
                .append(latitude)
                .append(",")
                .append(longitude)
                .append("&query_place_id=")
                .append(address);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuffer.toString()));
        ActivityUtils.startActivity(intent);
    }

    public static boolean hasMap(String pkg){
        try {
            return PackageUtils.getInstalledApplications().contains(pkg);
        }catch (Exception e){
            return false;
        }
    }
}
