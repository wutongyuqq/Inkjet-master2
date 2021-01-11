package me.samlss.inkjet.ui.fragments;

import android.Manifest;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import me.samlss.framework.permission.AppPermissionUtil;
import me.samlss.framework.permission.PermissionRequester;
import me.samlss.framework.permission.RequestListener;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.constant.EventBusDef;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.dialogs.DialogUtils;
import me.samlss.ui.widget.QMUITopBarLayout;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description QRCode fragment
 */
//StockOutFragment 出库
public class ScanQRCodeFragment extends BaseFragment {
    @BindView(R.id.zxingview)
    ZXingView mZXingView;

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    private String mQrcodeContent;
    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_qr_code, null);
        ButterKnife.bind(this, layout);

        initTopBar();
        initZXingView();
        return layout;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> popBackStack());
        mTopBar.setBackgroundColor(Color.TRANSPARENT);
        mTopBar.setTitle(R.string.qr_bar_code);
    }

    @Override
    public void onDestroy() {
        mZXingView.onDestroy();
        super.onDestroy();
    }

    private void initZXingView(){
         mZXingView.setDelegate(mQrCodeViewDelegate);
        startCamera();
    }

    private void startCamera(){
        if (AppPermissionUtil.hasPermission(Manifest.permission.CAMERA)){
            startScanning();
        }else{
            PermissionRequester.want(Manifest.permission.CAMERA)
                    .rationale((permission, requestExecutor) -> DialogUtils.showRejectedPermissionDialog(getActivity(), requestExecutor,
                            ResourceUtils.getString(R.string.reject_camera_permission)))
                    .listen(new RequestListener() {
                        @Override
                        public void onGranted(List<String> grantedPermissions) {
                            startScanning();
                        }

                        @Override
                        public void onDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions) {
                            onPermissionDenied(deniedPermissions, foreverDeniedPermissions);
                        }
                    })
                    .request();
        }
    }

    private void onPermissionDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions){
        if (!foreverDeniedPermissions.isEmpty()){
            DialogUtils.showRejectedPermissionGotToAppSettingDialog(getActivity(),
                    ResourceUtils.getString(R.string.camera_permission_forever_denied_tips));
            return;
        }

        if (!deniedPermissions.isEmpty()){
            DialogUtils.showRejectedPermissionTip(getActivity(),
                    ResourceUtils.getString(R.string.rejected_camera_permission_tip));
        }
    }

    private void startScanning(){
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    private void onScanSuccess(String result){
        mQrcodeContent = result;
        if (TextUtils.isEmpty(result)){
            ToastUtils.showShort(R.string.qrcode_empty);
            mZXingView.startSpot();
            return;
        }

        getBaseFragmentActivity().getSupportFragmentManager().popBackStackImmediate();
        EventBus.getDefault().post(new EventBusDef.BusBean(EventBusDef.FLAG_QRCODE_RESULT, result));
    }

    private QRCodeView.Delegate mQrCodeViewDelegate = new QRCodeView.Delegate() {
        @Override
        public void onScanQRCodeSuccess(String result) {
            onScanSuccess(result);
        }

        @Override
        public void onCameraAmbientBrightnessChanged(boolean isDark) {
            // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
            String tipText = mZXingView.getScanBoxView().getTipText();
            String ambientBrightnessTip = "\n环境过暗，请打开闪光灯~";

            if (isDark) {
                if (tipText != null && !tipText.contains(ambientBrightnessTip)) {
                    mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
                }
            } else {
                if (tipText != null && tipText.contains(ambientBrightnessTip)) {
                    tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                    mZXingView.getScanBoxView().setTipText(tipText);
                }
            }
        }

        @Override
        public void onScanQRCodeOpenCameraError() {
            ToastUtils.showShort(R.string.open_camera_failed);
        }
    };
}
