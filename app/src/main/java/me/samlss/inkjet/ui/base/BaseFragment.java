package me.samlss.inkjet.ui.base;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.EventBusDef;
import me.samlss.inkjet.utils.ImageUtils;
import me.samlss.inkjet.utils.InkjetUtils;
import me.samlss.inkjet.utils.UmengUtils;
import me.samlss.inkjet.utils.Utils;
import me.samlss.ui.arch.QMUIFragment;
import me.samlss.ui.util.QMUIDisplayHelper;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description base fragment
 */
public abstract class BaseFragment extends QMUIFragment {
    private ImageUtils.ImageTask mLoadImageTask;
    private ImageView mBannerView;

    @Override
    protected int backViewInitOffset() {
        return QMUIDisplayHelper.dp2px(getContext(), 100);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        UmengUtils.doPagePause(getClass().getSimpleName());
    }

    @Override
    public void onResume() {
        super.onResume();
        UmengUtils.doPageResume(getClass().getSimpleName());
        InkjetUtils.checkLogin();
        Utils.checkIfAutoConnectBt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoadImageTask != null && !mLoadImageTask.isCanceled()){
            mLoadImageTask.cancel();
        }
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    public void initializeBanner(ImageView bannerView){
        this.mBannerView = bannerView;
        if (bannerView != null){
            mLoadImageTask = ImageUtils.showImage(InkConfig.getCacheCompanyBannerUrl(), bannerView);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateHeader(EventBusDef.BusBean busBean){
        if (busBean.flag == EventBusDef.FLAG_UPDATE_HEADER){

            if (getActivity() == null || getActivity().isFinishing()){
                return;
            }

        }
        initializeBanner(this.mBannerView);
    }
}
