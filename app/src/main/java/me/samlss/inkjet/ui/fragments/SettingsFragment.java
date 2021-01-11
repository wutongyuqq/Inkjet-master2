package me.samlss.inkjet.ui.fragments;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.poi.ss.formula.functions.Even;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ActivityUtils;
import me.samlss.framework.utils.AppUtils;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.NetworkUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.api.Api;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.EventBusDef;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.LoginActivity;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.utils.ImageUtils;
import me.samlss.inkjet.utils.Utils;
import me.samlss.okhttp.OkCallBack;
import me.samlss.ui.widget.dialog.QMUITipDialog;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 设置
 */
public class SettingsFragment extends BaseFragment {
    private Call mUpdateAuthCall;
    private QMUITipDialog mUpdatingAuthDialog;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting, null);

        ButterKnife.bind(this, layout);
        if (InkConfig.getCompanyMode() == 1){
            layout.findViewById(R.id.btn_change_psw).setVisibility(View.GONE);
            layout.findViewById(R.id.btn_logout).setVisibility(View.GONE);
            layout.findViewById(R.id.btn_logout).setVisibility(View.GONE);
        }

        ViewUtils.setBackground(layout.findViewById(R.id.btn_change_psw),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_version_info),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_logout),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_verify_company_code),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_update_auth),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));
        initializeBanner(layout.findViewById(R.id.iv_header));
        return layout;
    }

    private void onChangePsw(){
        startFragment(new ChangePswFragment());
    }

    private void onShowVersionInfo(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.version_info)
                .setMessage(ResourceUtils.getString(R.string.app_name) + "-1.0版本" )
                .show();

    }

    private void onLogout(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.logout_or_not)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    UserManager.getInstance().setUserId(null);
                    InkConfig.setAutoLogin(false);
                    ActivityUtils.finishAllActivities();
                    ActivityUtils.startActivity(LoginActivity.class);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void onReVerifyCompanyCode(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.verify_company_code_or_not)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    UserManager.getInstance().setUserId(null);
                    //删除图片缓存文件夹
                    ImageUtils.deleteCacheImageDir();
                    //删除图片内存缓存
                    ImageUtils.deleteCacheMemory();

                    InkConfig.setLastConnectedBt(null);
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
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void postUpdateAuthFailed(String msg){
        if (mUpdatingAuthDialog != null){
            mUpdatingAuthDialog.dismiss();
        }
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(msg == null ? "更新授权失败！" : msg);
                }
            });
        }
    }


    private void postUpdateAuthSuccess(){
        if (mUpdatingAuthDialog != null){
            mUpdatingAuthDialog.dismiss();
        }
        EventBus.getDefault().post(new EventBusDef.BusBean(EventBusDef.FLAG_UPDATE_HEADER, null));
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("更新授权成功~");
                }
            });
        }
    }

    private void onUpdateAuth() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage("是否更新授权？")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (!NetworkUtils.isConnected()){
                        ToastUtils.showShort(R.string.no_net);
                        return;
                    }

                    if (TextUtils.isEmpty(InkConfig.getCacheCompanyCode())){
                        postUpdateAuthFailed(null);
                        dialog.dismiss();
                        return;
                    }

                    mUpdatingAuthDialog = new QMUITipDialog.Builder(getActivity())
                            .setTipWord("正在更新...")
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                            .create();

                    mUpdatingAuthDialog.show();
                    try {
                        mUpdateAuthCall = Api.checkCompanyCode(InkConfig.getCacheCompanyCode(), new OkCallBack() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                postUpdateAuthFailed(null);
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
                                        String companyApi = jsonData.getString("company_api");
                                        int mode = jsonData.getIntValue("is_localhost");
                                        String expired = jsonData.getString("validity_time");

                                        InkConfig.setCacheCompanyHost(host);
                                        InkConfig.setCacheCompanyName(companyName);
                                        InkConfig.setCacheCompanyLogoUrl(logoUrl);
                                        InkConfig.setCacheCompanyBannerUrl(bannerUrl);
                                        InkConfig.setCompanyQRCodeUrl(companyApi);
                                        InkConfig.setCompanyMode(mode);

                                        ImageUtils.showImage(bannerUrl, new ImageView(getContext()));

                                        String limitedDeviceStr = jsonData.getString("company_bluetooth");
                                        InkConfig.setSupportDeviceList(Utils.parseLimitedDevices(limitedDeviceStr));
                                        InkConfig.setUploadPrintRecordFlag(jsonData.getInteger("is_upload"));
                                        InkConfig.setCompanyExpired(expired);

                                        postUpdateAuthSuccess();
                                    }else{
                                        postUpdateAuthFailed(jsonObject.getString("msg"));
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    postUpdateAuthFailed(null);
                                }
                            }
                        });
                    }catch (Exception e){
                        ToastUtils.showShort("更新授权失败");
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUpdateAuthCall != null && !mUpdateAuthCall.isCanceled()){
            mUpdateAuthCall.cancel();
        }
    }

    @OnClick({R.id.btn_change_psw, R.id.btn_version_info, R.id.btn_logout, R.id.btn_verify_company_code, R.id.btn_update_auth})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_change_psw:
                onChangePsw();
                break;

            case R.id.btn_version_info:
                onShowVersionInfo();
                break;

            case R.id.btn_logout:
                onLogout();
                break;

            case R.id.btn_verify_company_code:
                onReVerifyCompanyCode();
                break;

            case R.id.btn_update_auth:
                onUpdateAuth();
                break;
        }
    }
}
