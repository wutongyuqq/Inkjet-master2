package me.samlss.inkjet.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.bean.ScanResultBean;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.WifiManager;
import me.samlss.inkjet.ui.adapters.ScanResultAdapter;
import me.samlss.inkjet.ui.dialogs.PrintDialog1;
import me.samlss.inkjet.ui.dialogs.PrintDialog2;
import me.samlss.inkjet.utils.BluetoothUtils;
import me.samlss.inkjet.utils.ExcelUtils;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description To display and print the scan results.
 */
public class ScanPrintListFragment extends BasePrintFragment {
    public static final String PARAM_LIST_KEY = "results";

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.btn_handle_device)
    Button mBtnHandleDevice;

    private ScanResultAdapter  mAdapter;
    private List<ScanResultBean> mScanResultList = new ArrayList<>();
    private SparseArray<Boolean> mSelectedItems = new SparseArray<>();

    private PrintDialog1 mPrintDialog1;
    private PrintDialog2 mPrintDialog2;
    private PrintBean mCurrentPrintBean;

    @Override
    protected View onCreateView() {
        if (getArguments() != null){
            mScanResultList.addAll((ArrayList) getArguments().getSerializable(PARAM_LIST_KEY));
            getArguments().clear();
            for (int i = 0; i < mScanResultList.size(); i++){
                mSelectedItems.put(i, true);
            }
        }
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_scan_print_list, null);
        ViewUtils.setBackground(layout.findViewById(R.id.btn_print),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ButterKnife.bind(this, layout);
        initialize();

        initDialogs();
        initializeBanner(layout.findViewById(R.id.iv_header));
        initializeRecyclerViews();
        mBtConnectMgr.addListener(mBtConnectListener);
        return layout;
    }

    private void initDialogs() {
        mPrintDialog1 = new PrintDialog1(getActivity(), this);
        mPrintDialog2 = new PrintDialog2(getActivity(), this);
    }

    private void initializeRecyclerViews(){
        mAdapter = new ScanResultAdapter(R.layout.layout_scan_item, mScanResultList, mSelectedItems);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (mSelectedItems.get(position, false)){
                mSelectedItems.remove(position);
            }else{
                mSelectedItems.put(position, true);
            }

            mAdapter.notifyItemChanged(position);
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showEmptyDialog(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.scan_result_null)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ListUtils.isEmpty(mScanResultList)){
            showEmptyDialog();
        }else{
            showTip();
        }

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
        if (mPrintDialog1 != null && mPrintDialog1.isShowing()) {
            mPrintDialog1.dismiss();
        }

        if (mPrintDialog2 != null && mPrintDialog2.isShowing()) {
            mPrintDialog2.dismiss();
        }

        mScanResultList.clear();
        mAdapter.notifyDataSetChanged();
        mSelectedItems.clear();
        mBtConnectMgr.removeListener(mBtConnectListener);
    }

    public boolean printFinish() {
        ToastUtils.showShort(R.string.print_state_finish);
        try {
            if (mCurrentPrintBean != null) {
                mPrintModel.printDone(ExcelUtils.getPrintContent(mCurrentPrintBean),
                        System.currentTimeMillis() - printConsumedTime);
            }
//            mHandler.removeCallbacks(test);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void cancelPrintTask() {

    }

    private void showTip(){
        if (InkConfig.isNoMoreTipForScanList()){
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.scan_list_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .setNegativeButton(R.string.no_more_tip, (dialog, which) -> {
                    dialog.dismiss();
                    InkConfig.setNoMoreTipForScanList(true);
                })
                .show();
    }

    private void onPrint(){
        if (ListUtils.isEmpty(mScanResultList)){
            showEmptyDialog();
            return;
        }

        if (mSelectedItems.size() == 0) {
            ToastUtils.showLong(R.string.scan_list_can_no_be_empty);
            return;
        }

        List<String> printContents = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++){
            printContents.add(mScanResultList.get(mSelectedItems.keyAt(i)).getValue());
        }

        executePrint(JSON.toJSONString(printContents));
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

    private void executePrint(String splitJson) {
        if (this.mBtConnectMgr == null || this.mBtConnectMgr.getState() != BTConnectManager.STATE_CONNECTED) {
            ToastUtils.showShort(R.string.no_connected_bt_device);
            return;
        }

        mCurrentPrintBean = new PrintBean();
        mCurrentPrintBean.setState(InkConstant.PRINT_STATE_NONE);
        mCurrentPrintBean.setSplits(splitJson);

        try {
            if (InkConfig.isAutoWifi()) {
                QMUITipDialog loading = new QMUITipDialog.Builder(getActivity())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("正在通过wifi创建项目，请稍等...")
                        .create();
                loading.setCancelable(false);
                loading.show();

                List<String> contents = JSON.parseArray(splitJson, String.class);
                if (ListUtils.isEmpty(contents)){
                    ToastUtils.showShort(R.string.no_print_data);
                    return;
                }

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
                            mPrintDialog1.show(mCurrentPrintBean);
                            printConsumedTime = System.currentTimeMillis();
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
                printConsumedTime = System.currentTimeMillis();
                mPrintDialog2.show(mCurrentPrintBean);
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
                if (result.equals(InkConfig.getPrintStartReturnValue())){
                    ToastUtils.showShort(R.string.start_print_des);
                }else if (result.equals(InkConfig.getPrintFinishReturnValue())){
                    ToastUtils.showShort(R.string.finish_print_des);
                    if (mPrintDialog1.isShowing()){
                        mPrintDialog1.receivedFinishedMsg();
                    }

                    if (mPrintDialog2.isShowing()){
                        mPrintDialog2.receivedFinishedMsg();
                    }
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

