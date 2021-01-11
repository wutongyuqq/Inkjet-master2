package me.samlss.inkjet.ui.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.api.Api;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.fragments.ChangePswFragment;
import me.samlss.okhttp.OkCallBack;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class ChangePswModel {
    private ChangePswFragment mChangePswFragment;
    private Call mChangePswCall;

    public ChangePswModel(ChangePswFragment changePswFragment){
        mChangePswFragment = changePswFragment;
    }

    private void postChangePswFailure(String msg){
        mChangePswFragment.getActivity().runOnUiThread(() -> mChangePswFragment.onChangePswFailure(msg));
    }

    private void postChangePswSuccess(){
        mChangePswFragment.getActivity().runOnUiThread(() -> mChangePswFragment.onChangePswSuccess());
    }

    private void cancelChangePswCall(){
        if (mChangePswCall != null && !mChangePswCall.isCanceled()){
            mChangePswCall.cancel();
        }
    }

    public void changePsw(String oldPsw, String newPsw){
        cancelChangePswCall();
        oldPsw = oldPsw.replace(" ", "");
        newPsw = newPsw.replace(" ", "");

        mChangePswCall = Api.updatePassword(oldPsw, newPsw, UserManager.getInstance().getUserId(), new OkCallBack() {
            @Override
            public void onFailure(Call call, IOException e) {
                postChangePswFailure(ResourceUtils.getString(R.string.change_psw_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                PLog.e("change psw result: "+result);
                try{
                    JSONObject jsonObject = JSON.parseObject(result);
                    String status = jsonObject.getString("status");
                    if (!TextUtils.isEmpty(status)
                            && status.equalsIgnoreCase("SUCCESS")){
                        postChangePswSuccess();
                    }else{
                        postChangePswFailure(ResourceUtils.getString(R.string.change_psw_failed));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    postChangePswFailure(ResourceUtils.getString(R.string.change_psw_failed));
                }
            }
        });
    }

    public void destroy(){
        cancelChangePswCall();
    }
}
