package me.samlss.inkjet.api;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import me.samlss.framework.log.PLog;
import me.samlss.framework.security.MD5;
import me.samlss.inkjet.BuildConfig;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.okhttp.OkCallBack;
import me.samlss.okhttp.OkHttp;
import me.samlss.so.SoCipher;
import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description Responsible for http interaction
 */
public class Api {
    private Api(){

    }

    /**
     * 初始化，可定义OkHttpClient
     * */
    public static void initialize(){
        OkHttp.init(new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build(),
                BuildConfig.DEBUG);
    }

    /**
     * 企业验证
     *
     * @param code 企业编码
     * @param callBack 回调
     * @return 返回call对象check company result
     * */
    public static Call checkCompanyCode(String code, OkCallBack callBack){
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        data.put("nowtime", getTimestamp());

        String dataJson = JSON.toJSONString(data);

        Map<String, String> params = new HashMap<>();
        params.put("data", dataJson);
        params.put("token", MD5.MD5(dataJson + SoCipher.get("h_k")));

        PLog.e("check code params: "+ JSON.toJSONString(params));
        return OkHttp.getHttp().post(SoCipher.get("b_u") + "checkCompanyCode.php", params, callBack);
    }

    /**
     * 登录
     *
     * @param userName 用户名
     * @param psw 密码
     * @param callBack 回调
     * @return 返回call对象
     * */
    public static Call login(String userName, String psw, String companyCode, OkCallBack callBack){
        Map<String, String> data = new HashMap<>();
        data.put("username", userName);
        data.put("password", MD5.MD5(psw));
        data.put("nowtime", getTimestamp());
        data.put("code", companyCode);

        String dataJson = JSON.toJSONString(data);

        Map<String, String> params = new HashMap<>();
        params.put("data", dataJson);
        params.put("token",  MD5.MD5(dataJson + SoCipher.get("h_k")));

        return OkHttp.getHttp().post(InkConfig.getCacheCompanyHost() + "checkUserLogin.php", params, callBack);
    }

    /**
     * 上传喷码记录
     *
     * @param coordinate GPS坐标
     * @param printing 喷码内容
     * @param userId 用户id
     * @param printTime 喷码时间(时间戳)
     * @param callBack 回调
     * @return 返回call对象
     * */
    public static Call uploadAnkAnnal(String coordinate, String printing, String userId, String printTime, OkCallBack callBack){
        Map<String, String> data = new HashMap<>();
        data.put("coordinate", coordinate);
        data.put("printing", printing);
        data.put("user_id", userId);
        data.put("printtime", printTime);

        String dataJson = JSON.toJSONString(data);

        Map<String, String> params = new HashMap<>();
        params.put("data", dataJson);
        params.put("token",  MD5.MD5(dataJson + SoCipher.get("h_k")));

        PLog.e("upload annal params: "+JSON.toJSONString(params));

        return OkHttp.getHttp().post(InkConfig.getCacheCompanyHost() + "checkAddPrinting.php", params, callBack);
    }

    /**
     * 上传喷码记录
     *
     * @param password 旧密码
     * @param newPassword 新密码
     * @param userId 用户id
     * @param callBack 回调
     * @return 返回call对象
     * */
    public static Call updatePassword(String password, String newPassword, String userId, OkCallBack callBack){
        Map<String, String> data = new HashMap<>();
        data.put("password", MD5.MD5(password));
        data.put("newPassword", MD5.MD5(newPassword));
        data.put("user_id", userId);
        data.put("nowtime", getTimestamp());

        String dataJson = JSON.toJSONString(data);

        Map<String, String> params = new HashMap<>();
        params.put("data", dataJson);
        params.put("token",  MD5.MD5(dataJson + SoCipher.get("h_k")));

        PLog.e("update psw params: "+JSON.toJSONString(params));
        return OkHttp.getHttp().post(InkConfig.getCacheCompanyHost() + "checkUpdateUserPassword.php", params, callBack);
    }

    public static Call getPrintData(String content, OkCallBack okCallBack){
        if (TextUtils.isEmpty(InkConfig.getCompanyQRCodeUrl())){
            if (okCallBack != null){
                okCallBack.onFailure(null, new IOException());
            }
            return null;
        }

        String url = InkConfig.getCompanyQRCodeUrl();
        try{
            url = url.replaceAll("QRCode", content);
        }catch (Exception e){
            e.printStackTrace();
        }

        return OkHttp.getHttp().get(url, null, okCallBack);
    }

    /**
     * 获取10位时间戳
     * */
    public static String getTimestamp(){
        long time = System.currentTimeMillis() / 1000;
        return String.valueOf(time);
    }
}
