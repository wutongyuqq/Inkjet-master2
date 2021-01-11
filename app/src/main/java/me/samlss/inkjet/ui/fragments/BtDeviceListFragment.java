package me.samlss.inkjet.ui.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.log.PLog;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.ui.adapters.BluetoothListAdapter;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.ui.widget.QMUITopBarLayout;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 蓝牙设备列表页
 */
public class BtDeviceListFragment extends BaseFragment {
    private static final boolean DEBUG = true;
    public static String DEVICE_ADDRESS = "device address";
    private static final String TAG = "DeviceList";

    private BluetoothAdapter mBtAdapter;

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    @BindView(R.id.recyclerView_available_bluetooth)
    RecyclerView mAvailableBtRecyclerView;

    @BindView(R.id.recyclerView_paired_bluetooth)
    RecyclerView mPairedBtRecyclerView;

    @BindView(R.id.tv_no_paired_bluetooth)
    TextView mTvNoPairedDevices;

    @BindView(R.id.tv_no_available_devices)
    TextView mTvNoAvailableDevices;

    @BindView(R.id.loading_progress)
    ContentLoadingProgressBar mLoadingBar;

    @BindView(R.id.button_scan)
    Button mBtnScan;

    private List<BluetoothDevice> mAvailableBtList = new ArrayList<>();
    private List<BluetoothDevice> mPairedBtList = new ArrayList<>();

    private BluetoothListAdapter mAvailableBtAdapter;
    private BluetoothListAdapter mPairedBtAdapter;

    @Override
    protected View onCreateView() {
        setFragmentResult(Activity.RESULT_CANCELED, null);
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_bt_device_list, null);
        ButterKnife.bind(this, layout);
        initTopBar();
        initDeviceList();
        updateBondedDevices();

        registerBluetoothReceiver();
        onDiscoveryBluetooth();
        checkIfShowSelectBtTips();
        return layout;
    }

    private void checkIfShowSelectBtTips(){
        if (InkConfig.isNoMoreTipsForSelectBt()){
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.select_bt)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .setNegativeButton(R.string.no_more_tip, (dialog, which) -> {
                    dialog.dismiss();
                    InkConfig.setNoMoreTipsForSelectBt(true);
                })
                .create().show();
    }

    private void registerBluetoothReceiver(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> popBackStack());
        mTopBar.setTitle(R.string.bluetooth);
    }

    private void initDeviceList(){
        mAvailableBtAdapter = new BluetoothListAdapter(R.layout.layout_item_bt, mAvailableBtList);
        mPairedBtAdapter = new BluetoothListAdapter(R.layout.layout_item_bt, mPairedBtList);

        mAvailableBtRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mPairedBtRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAvailableBtRecyclerView.setAdapter(mAvailableBtAdapter);
        mPairedBtRecyclerView.setAdapter(mPairedBtAdapter);

        mAvailableBtAdapter.setOnItemClickListener(mAvailableDevicesClickListener);
        mPairedBtAdapter.setOnItemClickListener(mPairedDevicesClickListener);
    }

    /**
     * 更新已配对设备
     * */
    private void updateBondedDevices(){
        Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            mTvNoPairedDevices.setVisibility(View.GONE);
            mPairedBtList.clear();
            mPairedBtList.addAll(pairedDevices);
            mPairedBtAdapter.notifyDataSetChanged();
        }else {
            mTvNoPairedDevices.setVisibility(View.VISIBLE);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                if (!mAvailableBtList.contains(device)){
                    mTvNoAvailableDevices.setVisibility(View.GONE);
                    mAvailableBtList.add(device);
                    mAvailableBtAdapter.notifyDataSetChanged();
                }
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                PLog.e("finish to discovery bt");
                mLoadingBar.setVisibility(View.GONE);
                mBtnScan.setText(R.string.search_bt_devices);
                if (mAvailableBtList.isEmpty()){
                    mTvNoAvailableDevices.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mBtAdapter != null) {
            this.mBtAdapter.cancelDiscovery();
        }
        getActivity().unregisterReceiver(this.mReceiver);
        mBtAdapter.cancelDiscovery();
    }

    private BaseQuickAdapter.OnItemClickListener mPairedDevicesClickListener = (adapter, view, position) -> {
        onResultPopBack(mPairedBtList.get(position));
    };

    private BaseQuickAdapter.OnItemClickListener mAvailableDevicesClickListener = (adapter, view, position) -> {
        onResultPopBack(mAvailableBtList.get(position));
    };

    private void onResultPopBack(BluetoothDevice bluetoothDevice){
        if (bluetoothDevice == null){
            return;
        }

        Intent data = new Intent();
        data.putExtra(PrintFragment.KEY_CONNECT_BT_DEVICE, bluetoothDevice);
        setFragmentResult(Activity.RESULT_OK, data);
        popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!this.mBtAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), PrintFragment.REQUEST_CODE_ENABLE_BLUETOOTH);
            return;
        }

        updateBondedDevices();
        if (!mBtAdapter.isDiscovering()) {
            mBtAdapter.startDiscovery();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PrintFragment.REQUEST_CODE_ENABLE_BLUETOOTH){
            if (resultCode != Activity.RESULT_OK){
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.warn)
                        .setMessage(R.string.need_enable_blue_tooth)
                        .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }

    private void onDiscoveryBluetooth() {
        PLog.d(TAG, "doDiscovery()");
        mLoadingBar.setVisibility(View.VISIBLE);

        if (this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
            this.mBtnScan.setText(R.string.search_bt_devices);
            return;
        }

        this.mBtnScan.setText(R.string.stop_searching);
        this.mAvailableBtList.clear();
        mAvailableBtAdapter.notifyDataSetChanged();
        this.mBtAdapter.startDiscovery();
    }

    @OnClick({R.id.button_scan})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button_scan:
                onDiscoveryBluetooth();
                break;
        }
    }
}
