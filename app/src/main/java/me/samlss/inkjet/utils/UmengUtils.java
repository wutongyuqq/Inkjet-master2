package me.samlss.inkjet.utils;

import android.app.Activity;
import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.Map;

import me.samlss.framework.utils.AppUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description utils of umeng
 */
public class UmengUtils {
    private UmengUtils(){
        throw new UnsupportedOperationException("Can not be instantiated.");
    }

    public static void initialize(Context context, String channel){
        UMConfigure.setLogEnabled(true);
        //UMConfigure.init(Context context, String appkey, String channel, int deviceType, String pushSecret);
        UMConfigure.init(context, "5ce290ef0cafb26057000681", channel, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setScenarioType(context, MobclickAgent.EScenarioType.E_UM_NORMAL);
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    public static void doResume(Activity activity)
    {
        MobclickAgent.onResume(activity);
    }

    public static void doPause(Activity activity)
    {
        MobclickAgent.onPause(activity);
    }

    public static void doPageResume(String pageName){
        MobclickAgent.onPageStart(pageName);
    }

    public static void doPagePause(String pageName){
        MobclickAgent.onPageEnd(pageName);
    }

    /**
     * 友盟计算事件
     * */
    public static void onEvent(String eventID, Map<String, String> map){
        MobclickAgent.onEvent(AppUtils.getApp(), eventID, map);
    }
}
