package me.samlss.inkjet.ui.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.api.Api;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.LoginActivity;
import me.samlss.inkjet.utils.Utils;
import me.samlss.okhttp.OkCallBack;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 登录逻辑类
 */
public class LoginModel {
    private WeakReference<LoginActivity> mLoginActivityRef;
    private Call mLoginRequestCall;
    private Call mCheckCompanyCodeCall;
    public LoginModel(LoginActivity loginActivity){
        mLoginActivityRef = new WeakReference<>(loginActivity);
    }

    /**
     * 开始登录
     * */
    public void login(String userName, String password, String companyCode){
        userName = userName.replace(" ", "");
        password = password.replace(" ", "");

        cancelLoginRequest();
        mLoginRequestCall = Api.login(userName, password, companyCode, new OkCallBack() {
            @Override
            public void onFailure(Call call, final IOException e) {
                postLoginFailure(ResourceUtils.getString(R.string.login_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                PLog.e("login result: "+result);
                try{
                    JSONObject jsonObject = JSON.parseObject(result);
                    String msg = jsonObject.getString("msg");
                    String status = jsonObject.getString("status");
                    if (status.equalsIgnoreCase("SUCCESS")){
                        String data = jsonObject.getString("data");
                        JSONObject jsonData =JSON.parseObject(data);
                        String userId = jsonData.getString("user_id");

                        if (TextUtils.isEmpty(data)
                                || TextUtils.isEmpty(status)
                                || TextUtils.isEmpty(userId)){
                            postCheckCompanyCodeFailure(msg);
                            return;
                        }

                        UserManager.getInstance().setUserId(userId);
                        postLoginSuccess();
                    }else{
                        postCheckCompanyCodeFailure(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    postLoginFailure(ResourceUtils.getString(R.string.login_failed));
                }
            }
        });
    }

    private void postExpired(){
        if (mLoginActivityRef.get() == null){
            return;
        }
        mLoginActivityRef.get().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginActivityRef.get().onCheckCompanyCodeSuccessButExpired();
            }
        });
    }

    private void postLoginFailure(final String msg){
        if (mLoginActivityRef.get() == null){
            return;
        }

        mLoginActivityRef.get().runOnUiThread(() ->  mLoginActivityRef.get().onLoginFailure(msg));
    }

    private void postLoginSuccess(){
        if (mLoginActivityRef.get() == null){
            return;
        }

        mLoginActivityRef.get().runOnUiThread(() ->  mLoginActivityRef.get().onLoginSuccess());
    }

    private void postCheckCompanyCodeFailure(final String msg){
        if (mLoginActivityRef.get() == null){
            return;
        }

        mLoginActivityRef.get().runOnUiThread(() -> mLoginActivityRef.get().onCheckCompanyCodeFailure(msg));
    }

    private void postCheckCompanyCodeSuccess(final String companyHost,
                                             final String companyName,
                                             final String companyLogoUrl,
                                             final String bannerUrl,
                                             final String companyQrCodeUrl){
        if (mLoginActivityRef.get() == null){
            return;
        }

        mLoginActivityRef.get().runOnUiThread(() -> mLoginActivityRef.get().onCheckCompanyCodeSuccess(companyHost, companyName, companyLogoUrl, bannerUrl, companyQrCodeUrl));
    }

    public void checkCompanyCode(String code){
        cancelCheckCompanyCodeRequest();
        mCheckCompanyCodeCall = Api.checkCompanyCode(code, new OkCallBack() {
            @Override
            public void onFailure(Call call, final IOException e) {
                postCheckCompanyCodeFailure(ResourceUtils.getString(R.string.check_company_code_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                PLog.e("check company result: "+result);
                try{
                    JSONObject jsonObject = JSON.parseObject(result);
                    String status = jsonObject.getString("status");
                    String data = jsonObject.getString("data");

                    if (!TextUtils.isEmpty(data)
                            && !TextUtils.isEmpty(status)
                            && status.equalsIgnoreCase("SUCCESS")){
                        JSONObject jsonData =JSON.parseObject(data);

                        String host = jsonData.getString("company_host");
                        String companyName = jsonData.getString("company_name");
                        String logoUrl = jsonData.getString("company_logo");
                        String bannerUrl = jsonData.getString("company_banner");
                        String limitedDeviceStr = jsonData.getString("company_bluetooth");
                        String companyApi = jsonData.getString("company_api");
                        String expired = jsonData.getString("validity_time");

                        try{
                            long expiredTime = Long.valueOf(expired);
                            long nowTime = System.currentTimeMillis() / 1000;
                            if (nowTime > expiredTime){
                                postExpired();
                                return;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        InkConfig.setSupportDeviceList(Utils.parseLimitedDevices(limitedDeviceStr));
                        InkConfig.setUploadPrintRecordFlag(jsonData.getInteger("is_upload"));
                        InkConfig.setCompanyMode(jsonData.getIntValue("is_localhost"));
                        InkConfig.setCompanyExpired(expired);

                        postCheckCompanyCodeSuccess(host, companyName, logoUrl, bannerUrl, companyApi);
                    }else{
                        postCheckCompanyCodeFailure(jsonObject.getString("msg"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    postCheckCompanyCodeFailure(ResourceUtils.getString(R.string.check_company_code_failed));
                }
            }
        });
    }

    private void cancelLoginRequest(){
        if (mLoginRequestCall != null
                && !mLoginRequestCall.isCanceled()){
            mLoginRequestCall.cancel();
        }
    }

    private void cancelCheckCompanyCodeRequest(){
        if (mCheckCompanyCodeCall != null
                && !mCheckCompanyCodeCall.isCanceled()){
            mCheckCompanyCodeCall.cancel();
        }
    }

    public void destroy(){
        mLoginActivityRef.clear();
        cancelLoginRequest();
        cancelCheckCompanyCodeRequest();
    }
}
