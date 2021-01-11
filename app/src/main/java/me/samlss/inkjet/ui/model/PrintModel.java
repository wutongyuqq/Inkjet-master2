package me.samlss.inkjet.ui.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.api.Api;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.InkAnnal;
import me.samlss.inkjet.managers.LocationManager;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.utils.UmengUtils;
import me.samlss.okhttp.OkCallBack;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码逻辑类
 */
public class PrintModel {
    public PrintModel(){

    }

    /**
     * 喷码完毕
     *
     * @param content 喷码内容
     * @param consumedTime 喷码总耗时
     * */
    public void printDone(String content, long consumedTime){
        PLog.e("喷码结束, 花费了: "+consumedTime+", 内容："+content);
        String location = LocationManager.getGPSLocation();

        String address = "";
        if (LocationManager.getCurrentLocation() != null){
            address = LocationManager.getCurrentLocation().getAoiName();
            if (address == null){
                address = LocationManager.getCurrentLocation().getAddress();
            }
        }

        if (address == null){
            address = "";
        }

        InkAnnal inkAnnal = new InkAnnal();
        inkAnnal.setPrint_time(System.currentTimeMillis());
        inkAnnal.setContent(content);
        inkAnnal.setPrint_consumed_time(consumedTime); //消耗时间
        inkAnnal.setAddress(address);
        inkAnnal.setLatitude(LocationManager.getCurrentLocation() == null ? 0 : LocationManager.getCurrentLocation().getLatitude());
        inkAnnal.setLongitude(LocationManager.getCurrentLocation() == null ? 0 : LocationManager.getCurrentLocation().getLongitude());
        inkAnnal.setUser_id(UserManager.getInstance().getCompanyUserId());

        ThreadUtils.executeByIo(new ThreadUtils.Task<Void>() {
            @Nullable
            @Override
            public Void doInBackground() throws Throwable {
                DbManager.get().insertInkAnnal(inkAnnal);
                return null;
            }

            @Override
            public void onSuccess(@Nullable Void result) {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onFail(Throwable t) {
            }
        });

        Map<String, String> umEv = new HashMap<>();
        umEv.put("time", String.valueOf(inkAnnal.getPrint_time()));
        umEv.put("content", content);
        umEv.put("address", address);
        umEv.put("user_id", UserManager.getInstance().getCompanyUserId());
        UmengUtils.onEvent("10000", umEv);

        if (InkConfig.getUploadPrintRecordFlag() == InkConstant.NOT_UPLOAD_PRINT_RECORD){
            PLog.e("no upload print record.");
            return;
        }

        PLog.e("upload print record.");
        Api.uploadAnkAnnal(location, content, UserManager.getInstance().getUserId(), Api.getTimestamp(), new OkCallBack() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                PLog.e("upload ank annal result: "+result);
            }
        });
    }
}
