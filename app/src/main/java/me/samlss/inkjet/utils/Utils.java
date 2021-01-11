package me.samlss.inkjet.utils;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ActivityUtils;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.LoginActivity;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description utils of this app
 */
public class Utils {
    private Utils(){}

    public static Set<String> parseLimitedDevices(String deviceStr){
        try{
            Set<String> strings = new HashSet<>();
            String[] limitedDevices = deviceStr.split("\n");
            for (String device: limitedDevices){
                strings.add(device);
            }

            return strings;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static void checkIfAutoConnectBt(){
        BTConnectManager btConnectManager = BTConnectManager.getInstance();
        String lastConnectedBt;
        if (!TextUtils.isEmpty(lastConnectedBt = InkConfig.getLastConnectedBt())
                && !btConnectManager.isConnected()
                && isBtValid(lastConnectedBt)){
            btConnectManager.connectBluetooth(lastConnectedBt);
        }else{
            if (btConnectManager != null
                    && btConnectManager.getState() == BTConnectManager.STATE_NONE
                    && btConnectManager.isEnabled()) {
                btConnectManager.scheduleAcceptWait();
            }
        }
    }


    public static String replaceBlank(String src) {
        String dest = "";
        if (src != null) {
            Pattern pattern = Pattern.compile("\t|\r|\n|\\s*");
            Matcher matcher = pattern.matcher(src);
            dest = matcher.replaceAll("");
        }
        return dest;
    }


    /**
     * 判断蓝牙是否可用
     * */
    public static boolean isBtValid(String device){
        if (TextUtils.isEmpty(device)){
            return false;
        }

        device = replaceBlank(device);

        Set<String> supportDevices = InkConfig.getSupportDeviceList();
        if (supportDevices != null && !supportDevices.isEmpty()){
            for (String sDevice : supportDevices){
                if (device.equalsIgnoreCase(replaceBlank(sDevice))){
                    return true;
                }
            }
        }

        return false;
    }

    public static int getInt(Integer integer){
        return getInt(integer, 0);
    }

    public static int getInt(Integer integer, int defaultInt){
        return integer == null ? defaultInt : integer;
    }


    public static void changeCompanyCode(){
        UserManager.getInstance().setUserId(null);
        //删除图片缓存文件夹
        ImageUtils.deleteCacheImageDir();
        //删除图片内存缓存
        ImageUtils.deleteCacheMemory();

        InkConfig.setAutoLogin(false);
        InkConfig.setCachePassword("");
        InkConfig.setCacheCompanyCode("");
        InkConfig.setCompanyExpired("");
        InkConfig.setCacheAccount("");
        InkConfig.setCacheCompanyHost("");
        InkConfig.setCacheCompanyLogoUrl("");
        InkConfig.setCacheCompanyBannerUrl("");
        InkConfig.setCacheCompanyName("");

        ActivityUtils.finishAllActivities();
        ActivityUtils.startActivity(LoginActivity.class);
    }

    public static String subString(String str, String strStart, String strEnd) {
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.lastIndexOf(strEnd);

        return str.substring(strStartIndex, strEndIndex).substring(strStart.length());
    }

    public static String unicodeToString(String unicode) {

        String str = unicode.replace("0x", "\\");

        StringBuffer string = new StringBuffer();
        String[] hex = str.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            string.append((char) data);
        }
        return string.toString();
    }
}
