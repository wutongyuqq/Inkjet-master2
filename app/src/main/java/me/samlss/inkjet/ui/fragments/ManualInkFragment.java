package me.samlss.inkjet.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.WifiManager;
import me.samlss.inkjet.utils.BluetoothUtils;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 手输喷码
 */
public class ManualInkFragment extends BasePrintFragment {
    @BindView(R.id.et_input)
    EditText mEtInput;

    private AlertDialog mPrintingDialog;
    private String mPrintContent;
    @BindView(R.id.btn_handle_device)
    Button mBtnHandleDevice;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_manual_ink, null);
        ViewUtils.setBackground(layout.findViewById(R.id.btn_print),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ButterKnife.bind(this, layout);
        initialize();

        mEtInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                KeyboardUtils.hideSoftInput(mEtInput);
            }
            return (actionId == EditorInfo.IME_ACTION_DONE);
        });

        initializeBanner(layout.findViewById(R.id.iv_header));
        mBtConnectMgr.addListener(mBtConnectListener);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setBackground(mBtnHandleDevice, DrawableUtils.getSelectedSelector(DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.qmui_config_color_gray_5), DensityUtils.dp2px(5))));

        mBtnHandleDevice.setOnClickListener(v -> handleDevice());
        mBtnHandleDevice.setSelected(mBtConnectMgr.isConnected());
        if (mBtConnectMgr.isConnected()){
            setConnectedSuccessUI(mBtConnectMgr.getConnectedDevice());
        }else{
            setConnectedFailureUI();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBtConnectMgr.removeListener(mBtConnectListener);
    }

    @Override
    public boolean printFinish() {
        return false;
    }

    @Override
    public void cancelPrintTask() {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtInput);
        return super.dispatchTouchEvent(ev);
    }

    private void onPrint(){
        String text = mEtInput.getText().toString();
        if (TextUtils.isEmpty(text)){
            ToastUtils.showShort(R.string.input_print_content);
            return;
        }

        executePrint(text);
    }

    private void showAbortTaskDialog(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.abort_task_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    mPrintingDialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    if (mPrintingDialog != null) {
                        mPrintingDialog.show();
                    }
                })
                .show();
    }

    private void showFinishTaskDialog(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.finish_task_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    onPrintFinish();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    mPrintingDialog.show();
                })
                .show();
    }

    private void onPrintFinish(){
        if (mPrintingDialog.isShowing()){
            mPrintingDialog.dismiss();
        }

        mPrintModel.printDone(mPrintContent, System.currentTimeMillis() - printConsumedTime);
    }

    private void showCreateWifiProjFailTip(){
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.tip)
                    .setMessage("Wifi创建项目失败，请重新尝试或在主页->喷码设置中检查喷码机IP是否设置且连接成功！")
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void executePrint(String content) {
        mPrintContent = content;
        if (this.mBtConnectMgr == null || this.mBtConnectMgr.getState() != BTConnectManager.STATE_CONNECTED) {
            ToastUtils.showShort(R.string.no_connected_bt_device);
            return;
        }

        if (mPrintingDialog != null && mPrintingDialog.isShowing()){
            mPrintingDialog.dismiss();
        }

        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_dialog_loading, null);
        ((TextView)contentView.findViewById(R.id.tv_msg)).setText(ResourceUtils.getString(R.string.printing, content));
        mPrintingDialog = new AlertDialog.Builder(getActivity())
                .setView(contentView)
                .setPositiveButton(R.string.ended, (dialog, which) -> {
                    dialog.dismiss();
                    showFinishTaskDialog();
                })
                .setNegativeButton(R.string.abort_task, (dialog, which) -> showAbortTaskDialog()).create();
        mPrintingDialog.setCancelable(false);

        printConsumedTime = System.currentTimeMillis();


        try {
            if (InkConfig.isAutoWifi()) {
                QMUITipDialog loading = new QMUITipDialog.Builder(getActivity())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("正在通过wifi创建项目，请稍等...")
                        .create();
                loading.setCancelable(false);
                loading.show();

                List<String> contents = new ArrayList<>();
                contents.add(content);

                final String prjName = "penmayi_"+System.currentTimeMillis();
                ThreadUtils.executeByIo(new ThreadUtils.Task<Boolean>() {
                    @Nullable
                    @Override
                    public Boolean doInBackground() throws Throwable {
                        long before = System.currentTimeMillis();

                        boolean ret = WifiManager.getInstance().createWifiProject(prjName, true, contents);
                        PLog.e("create proj in time: "+(System.currentTimeMillis() - before));
                        return ret;
                    }

                    @Override
                    public void onSuccess(@Nullable Boolean result) {
                        PLog.e("create: "+result);
                        loading.dismiss();
                        if (result != null && result){
                            WifiManager.getInstance().deleteWifiProjectAsync(InkConfig.getLastWifiPrjName(), true);
                            InkConfig.setLastWifiPrjName(prjName);
                            printConsumedTime = System.currentTimeMillis();
                            mPrintingDialog.show();
                            sendMessageWithStandardFormat(content);
                        }else{
                            showCreateWifiProjFailTip();
                        }
                    }

                    @Override
                    public void onCancel() {
                        loading.dismiss();
                    }

                    @Override
                    public void onFail(Throwable t) {
                        loading.dismiss();
                        showCreateWifiProjFailTip();
                    }
                });
            } else {
                mPrintingDialog.show();
                sendMessageWithStandardFormat(content);
            }
        }catch (Exception e){
            ToastUtils.showShort("喷码失败，请重新尝试！");
            e.printStackTrace();
        }

    }

    private void setConnectedFailureUI(){
        mConnectingDialog.dismiss();
        mBtnHandleDevice.setText(R.string.connect_device);
        mBtnHandleDevice.setSelected(false);

        if (mPrintingDialog != null){
            mPrintingDialog.dismiss();
        }
    }

    private void setConnectedSuccessUI(BluetoothDevice device){
        if (device == null){
            return;
        }

        mConnectingDialog.dismiss();
        mBtnHandleDevice.setSelected(true);
        mBtnHandleDevice.setText("已连接"+"(" + BluetoothUtils.getDisplayName(device) + ")");
    }

    private BTConnectManager.OnBtConnectListener mBtConnectListener = new BTConnectManager.OnBtConnectListener() {
        @Override
        public void onConnectionFailure(BluetoothDevice device) {
            setConnectedFailureUI();
            ToastUtils.showShort("与"+ BluetoothUtils.getDisplayName(device) + "连接失败~");
        }

        @Override
        public void onConnectionBreak(BluetoothDevice device) {
            setConnectedFailureUI();
            ToastUtils.showShort("与"+ BluetoothUtils.getDisplayName(device) + "连接断开~");
        }

        @Override
        public void onConnectionSuccess(BluetoothDevice device) {
            setConnectedSuccessUI(device);
            ToastUtils.showShort("已连接到设备：" + BluetoothUtils.getDisplayName(device));
        }

        @Override
        public void onReceiveData(BluetoothDevice device, int bytes, byte[] data) {
            try{
                String result = new String(data, 0, bytes, "utf-8");
                PLog.e("result: "+result);

                if (result.equals(InkConfig.getPrintStartReturnValue())){
                    ToastUtils.showShort(R.string.start_print_des);
                }else if (result.equals(InkConfig.getPrintFinishReturnValue())){
                    ToastUtils.showShort(R.string.finish_print_des);
                    onPrintFinish();
                } else if(result.equals(InkConfig.getPrintStopReturnValue())){
                    ToastUtils.showShort(R.string.stop_print_des);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @OnClick({R.id.btn_print})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_print:
                onPrint();
                break;
        }
    }
}
