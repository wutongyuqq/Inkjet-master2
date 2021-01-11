package me.samlss.inkjet.ui.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import me.samlss.framework.log.PLog;
import me.samlss.framework.permission.AppPermissionConstant;
import me.samlss.framework.permission.AppPermissionUtil;
import me.samlss.framework.permission.PermissionRequester;
import me.samlss.framework.permission.RequestListener;
import me.samlss.framework.utils.ArrayUtils;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.GotoUtils;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.LocationManager;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.model.PrintModel;
import me.samlss.inkjet.utils.Utils;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码的基础fragment
 */
public abstract class BasePrintFragment extends BaseFragment {
    public static final int REQUEST_CODE_START_DEVICE_LIST_PAGE = 133;
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 134;
    public static final String KEY_CONNECT_BT_DEVICE = "connect_device";

    private AlertDialog mLocationPermissionDeniedDialog;
    private AlertDialog  mRejectLocationPermissionDialog;
    private BluetoothAdapter mBluetoothAdapter = null;
    protected BTConnectManager mBtConnectMgr = null;
    long printConsumedTime; //记录喷码时间

    String[] mHexStringArray = new String[256];
    PrintModel mPrintModel;

    QMUITipDialog mConnectingDialog;


    public void initialize(){
        mBtConnectMgr = BTConnectManager.getInstance();
        initHexStringArray();
        initConnectingDialog();
        mPrintModel = new PrintModel();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mBluetoothAdapter == null) {
            ToastUtils.showShort(R.string.bluetooth_not_available);
            popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnectingDialog.isShowing()){
            mConnectingDialog.dismiss();
        }

        LocationManager.get().stopListening();
    }

    private void showUnSupportDeviceDialog(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warn)
                .setMessage(R.string.not_support_device_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onFragmentResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_START_DEVICE_LIST_PAGE){
            if (resultCode == Activity.RESULT_OK){
                if (data != null || data.getParcelableArrayExtra(KEY_CONNECT_BT_DEVICE) != null){
                    BluetoothDevice device = data.getParcelableExtra(KEY_CONNECT_BT_DEVICE);
                    if (device != null) {
                        if (!Utils.isBtValid(device.getAddress())){
                            showUnSupportDeviceDialog();
                            return;
                        }

                        if (mBtConnectMgr.isConnected()){
                            disconnectBluetooth();
                        }else{
                            connectBluetooth(device);
                        }
                    }
                }
            }
        }
    }

    public void sendMessageWithStandardFormat(String content) {
        try {
            content += Character.valueOf((char) InkConfig.getEndCharacter());
            this.mBtConnectMgr.write(content.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //原本顺序：AAA  -> BBB -> CCC
    //要调整为：CCC  -> AAA -> BBB
    //将最后一个放到第一个
    public void sendMessageWithSeparatorFormat(String ...contents) {
        if (ArrayUtils.isEmpty(contents)){
            return;
        }

        try {
            Character splitChar = Character.valueOf((char) InkConfig.getSplitCharacter());

            String sFinal = "";
            for (String content : contents){
                sFinal += content + splitChar;
            }

            int lastTabIndex;
            if (( lastTabIndex = sFinal.lastIndexOf(splitChar)) > 0){
                sFinal = sFinal.substring(0, lastTabIndex);
            }
            sFinal += Character.valueOf((char) InkConfig.getEndCharacter());
            this.mBtConnectMgr.write(sFinal.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageWithSeparatorFormat(List<String> contents) {
        if (ListUtils.isEmpty(contents)){
            return;
        }

        try {
            Character splitChar = Character.valueOf((char) InkConfig.getSplitCharacter());

            String sFinal = "";
            for (String content : contents){
                sFinal += content + splitChar;
            }

            int lastTabIndex;
            if (( lastTabIndex = sFinal.lastIndexOf(splitChar)) > 0){
                sFinal = sFinal.substring(0, lastTabIndex);
            }
            sFinal += Character.valueOf((char) InkConfig.getEndCharacter());
            PLog.e("s final: "+sFinal);
            this.mBtConnectMgr.write(sFinal.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void handleDevice(){
        if (mBtConnectMgr.isConnected()){
            disconnectBluetooth();
        }else{
            launchBluetoothDevices();
        }
    }

    private void initHexStringArray() {
        for (int i = 0; i < 256; i++) {
            if (i < 16) {
                this.mHexStringArray[i] = Integer.toHexString(i).toUpperCase();
            } else {
                this.mHexStringArray[i] = Integer.toHexString(i).toUpperCase();
            }
        }
    }

    private void initConnectingDialog(){
        mConnectingDialog = new QMUITipDialog.Builder(getActivity())
                .setTipWord(ResourceUtils.getString(R.string.connecting))
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mConnectingDialog.setCanceledOnTouchOutside(false);
    }

    public void launchBluetoothDevices(){
        if (!this.mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_CODE_ENABLE_BLUETOOTH);
            return;
        }

        startFragmentForResult(new BtDeviceListFragment(), REQUEST_CODE_START_DEVICE_LIST_PAGE);
    }

    private void connectBluetooth(BluetoothDevice device){
        if (device == null){
            return;
        }

        mBtConnectMgr.connectBluetooth(device);
        mConnectingDialog.show();
    }

    public void disconnectBluetooth(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.warn);
        builder.setMessage(R.string.disconnect_device_tip);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            if (mBtConnectMgr != null) {
                mBtConnectMgr.cancelAllBtThread();
            }
//            mBtnHandleDevice.setSelected(false);
//            mBtnHandleDevice.setText(R.string.connect_device);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void requestLocationPermission(){
        if (!AppPermissionUtil.hasPermission(AppPermissionConstant.LOCATION_GROUP)){
            if (InkConfig.isNoMoreTip4Location() || (mLocationPermissionDeniedDialog != null && mLocationPermissionDeniedDialog.isShowing())){
                return;
            }

            PermissionRequester.want(AppPermissionConstant.LOCATION_GROUP)
                    .listen(new RequestListener() {
                        @Override
                        public void onGranted(List<String> grantedPermissions) {
                            LocationManager.get().startListening();
                        }

                        @Override
                        public void onDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions) {
                            onLocationPermissionDenied(deniedPermissions, foreverDeniedPermissions);
                        }
                    })
                    .request();
        }else{
            LocationManager.get().startListening();
        }
    }

    private void onLocationPermissionDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions) {
        if (!foreverDeniedPermissions.isEmpty()){
            if (mLocationPermissionDeniedDialog == null) {
                mLocationPermissionDeniedDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_tips)
                        .setMessage(R.string.permission_forever_denied_tips_for_ink)
                        .setPositiveButton(R.string.jump, (dialog, which) -> {
                            dialog.dismiss();
                            GotoUtils.openAppDetailSettings();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setNeutralButton(R.string.no_more_tip, (dialog, which) -> {
                            dialog.dismiss();
                            InkConfig.setNoMoreTip4Location(true);
                        })
                        .setCancelable(false)
                        .create();
            }
            if (!mLocationPermissionDeniedDialog.isShowing() && !InkConfig.isNoMoreTip4Location()) {
                mLocationPermissionDeniedDialog.show();
            }
            return;
        }

        if (!deniedPermissions.isEmpty()){
            showRejectLocationPermissionDialog();
        }
    }

    private void showRejectLocationPermissionDialog(){
        if (mRejectLocationPermissionDialog == null) {
            mRejectLocationPermissionDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.warn)
                    .setMessage(R.string.rejected_permission_tip_for_ink)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        }
        if (!mRejectLocationPermissionDialog.isShowing()){
            mRejectLocationPermissionDialog.show();
        }
    }

    //true代表继续喷码
    public abstract boolean printFinish();

    public abstract void cancelPrintTask();
}
