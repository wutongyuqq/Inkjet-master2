package me.samlss.inkjet.managers;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import me.samlss.ebs.scanner.ScannerManager;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.NetworkUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.api.Api;
import me.samlss.inkjet.bean.ScanResultBean;
import me.samlss.inkjet.constant.EventBusDef;
import me.samlss.inkjet.ui.MainFragment;
import me.samlss.inkjet.ui.fragments.ScanPrintListFragment;
import me.samlss.inkjet.utils.Utils;
import me.samlss.okhttp.OkCallBack;
import me.samlss.ui.widget.dialog.QMUITipDialog;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 通过二维码扫码
 */
public class QRCodeManager {
    private QMUITipDialog mGettingDialog;
    private MainFragment mMainFragment;
    private Call mFetchPrintingDataRequest;

    public QRCodeManager(MainFragment fragment){
        mMainFragment = fragment;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ScannerManager.getInstance().addCallback(mScannerCallback);

        mGettingDialog = new QMUITipDialog.Builder(fragment.getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.getting_print_data))
                .create();

        mGettingDialog.setCanceledOnTouchOutside(false);
        mGettingDialog.setOnDismissListener(dialog -> cancelFetchPrintingDataRequest());
    }

    public void cancelFetchPrintingDataRequest(){
        if (mFetchPrintingDataRequest != null
                && !mFetchPrintingDataRequest.isCanceled()){
            mFetchPrintingDataRequest.cancel();
        }
    }

    public void destroy(){
        mMainFragment = null;
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }

        ScannerManager.getInstance().removeCallback(mScannerCallback);
    }

    private ScannerManager.Callback mScannerCallback = new ScannerManager.Callback() {
        @Override
        public void onScanResult(String result) {
            PLog.e("mScannerCallback => "+result);
            if (TextUtils.isEmpty(result)){
                ToastUtils.showShort("扫描枪扫描到的内容为空~");
                return;
            }

            fetchPrintingData(result);
        }
    };

    private void postFetchScanDataSuccess(ArrayList<ScanResultBean> scanResults){
        ThreadUtils.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mMainFragment != null && mMainFragment.getActivity() != null) {
                    mGettingDialog.dismiss();

                    if (ListUtils.isEmpty(scanResults)) {
                        postFetchScanDataFailure(null);
                        return;
                    }

                    ScanPrintListFragment scanPrintListFragment = new ScanPrintListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ScanPrintListFragment.PARAM_LIST_KEY, scanResults);
                    scanPrintListFragment.setArguments(bundle);
                    mMainFragment.startFragment(scanPrintListFragment);
                }
            }
        });
    }

    private void postFetchScanDataFailure(final String msg){
        ThreadUtils.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mMainFragment != null && mMainFragment.getActivity() != null){
                    mGettingDialog.dismiss();

                    String aMsg = msg;
                    if (msg == null){
                        aMsg = ResourceUtils.getString(R.string.scan_result_null);
                    }else{
                        aMsg = "获取喷码内容失败，错误原因："+aMsg;
                    }

                    new AlertDialog.Builder(mMainFragment.getActivity())
                            .setTitle(R.string.tip)
                            .setMessage(aMsg)
                            .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }
        });
    }

    private void fetchPrintingData(String content){
        if (mMainFragment == null || mMainFragment.getActivity() == null){
            return;
        }

        if (TextUtils.isEmpty(content)){
            new AlertDialog.Builder(mMainFragment.getActivity())
                    .setTitle(R.string.tip)
                    .setMessage(R.string.scan_content_empty)
                    .show();
            return;
        }

        if (!NetworkUtils.isConnected()){
            ToastUtils.showShort(R.string.no_net);
            return;
        }

        final QMUITipDialog dialog = new QMUITipDialog.Builder(mMainFragment.getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在获取扫码内容...")
                .create();
        dialog.show();
        cancelFetchPrintingDataRequest();

        mFetchPrintingDataRequest = Api.getPrintData(content, new OkCallBack() {
            @Override
            public void onFailure(Call call, IOException e) {
                mMainFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                String msg = (e == null ? "no msg" : e.getMessage());
                postFetchScanDataFailure(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mMainFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });

                String result = response.body().string();
                parseResult(result);
            }
        });
    }

    private void parseResult(String result){
        try{
            result = Utils.subString(result, "{", "}");
            String[] contents = result.split(",");

            ArrayList<ScanResultBean> scanResultBeans = new ArrayList<>();

            for (String s : contents){
                ScanResultBean scanResultBean = parseScanResult(s);
                if (scanResultBean != null){
                    scanResultBeans.add(scanResultBean);
                }
            }

            if (!ListUtils.isEmpty(scanResultBeans)){
                postFetchScanDataSuccess(scanResultBeans);
            }else{
                postFetchScanDataFailure(result);
            }
        }catch (Exception e){
            e.printStackTrace();
            postFetchScanDataFailure(null);
        }
    }

    private ScanResultBean parseScanResult(String section){
        if (TextUtils.isEmpty(section) || !section.contains(":")){
            return null;
        }


        try{
            String[] keyV = section.split(":");
            String key = Utils.subString(keyV[0], "\"", "\"");
            String value = keyV[1];
            if (value.startsWith("\"") && value.endsWith("\"")){
                value = Utils.subString(value, "\"", "\"");
            }else{
                int pIndex = value.indexOf(".");
                if (pIndex != -1){
                    value = value.substring(0, pIndex);
                }
            }

            if(TextUtils.isEmpty(key)){
                return null;
            }

            if (!TextUtils.isEmpty(value)){
                value = value.startsWith("\\u") ? Utils.unicodeToString(value) : value;
            }
            return new ScanResultBean(key, value);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQRCodeResult(EventBusDef.BusBean busBean){
        if (busBean.flag == EventBusDef.FLAG_QRCODE_RESULT){
            if (busBean.tag != null && busBean.tag instanceof String){
                fetchPrintingData((String) busBean.tag);
            }
        }
    }
}
