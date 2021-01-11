package me.samlss.inkjet.managers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ActivityUtils;
import me.samlss.framework.utils.PreconditionsUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.utils.Utils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 蓝牙管理器
 */
public class BTConnectManager {
    public static final int STATE_NONE = 0; //nothing
    public static final int STATE_ACCEPT_WAIT = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private static final String BT_NAME = "Inkjet";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String TAG = "bcm"; //BtConnectMgr
    private int mBtState = 0;
    private AcceptThread mAcceptThread;
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mMainThreadHandler;
    private BluetoothDevice mConnectedDevice;
    private List<OnBtConnectListener> mListeners = Collections.synchronizedList(new ArrayList<>());
    private static volatile BTConnectManager sInstance;

    private BTConnectManager() {
//        mConnectListener = connectListener;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }


    public boolean isEnabled(){
        return  mAdapter != null && mAdapter.isEnabled();
    }

    public static BTConnectManager getInstance(){
        if (sInstance == null){
            synchronized (BTConnectManager.class){
                if (sInstance == null){
                    sInstance = new BTConnectManager();
                }
            }
        }

        return sInstance;
    }


    public boolean isConnected() {
        return mBtState == STATE_CONNECTED;
    }

    //as server
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;
        private boolean isCanceled = false;

        public AcceptThread() {
            BluetoothServerSocket bss = null;
            try {
                bss = BTConnectManager.this.mAdapter.listenUsingRfcommWithServiceRecord(BTConnectManager.BT_NAME, BTConnectManager.MY_UUID);
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "listen() failed", e);
            }
            this.mServerSocket = bss;
        }

        public void run() {
            PLog.e(BTConnectManager.TAG, "Begin Bt accept thread.");
            setName("BtAcceptThread");
            while (mServerSocket != null
                    && !isCanceled
                    && BTConnectManager.this.mBtState != STATE_CONNECTED) {
                try {
                    BluetoothSocket socket = this.mServerSocket.accept();
                    if (socket != null) {
                        synchronized (BTConnectManager.this) {
                            switch (BTConnectManager.this.mBtState) {
                                case STATE_NONE:
                                case STATE_CONNECTED:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        cancel();
                                        PLog.e(BTConnectManager.TAG, "Could not close unwanted socket", e);
                                        break;
                                    }
                                case STATE_CONNECTING:
                                case STATE_ACCEPT_WAIT:
                                    BTConnectManager.this.onConnected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException e2) {
                    cancel();
                    PLog.e(BTConnectManager.TAG, "Accept bt failed", e2);
                }
            }
            PLog.e(BTConnectManager.TAG, "End Bt accept thread.");
        }

        public void cancel() {
            isCanceled = true;
            PLog.e(BTConnectManager.TAG, "Cancel " + this);
            try {
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "Close server socket failed", e);
            }
        }
    }

    //as client
    private class ConnectThread extends Thread {
        private final BluetoothDevice mBtDevice;
        private BluetoothSocket mBtSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mBtDevice = device;
            BluetoothSocket bs = null;
            try {
                bs = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "Create Rfcomm failed when connectBluetooth bt, error:", e);
            }
            this.mBtSocket = bs;
        }

        public void run() {
            mAdapter.cancelDiscovery();
            PLog.e(BTConnectManager.TAG, "Begin bt connectBluetooth thread");
            setName("BtConnectThread");
            try {
                this.mBtSocket.connect();
                synchronized (BTConnectManager.this) { //done connect task
                    mConnectThread = null;
                }

                onConnected(this.mBtSocket, this.mBtDevice);
            } catch (Exception e) {
                PLog.e(BTConnectManager.TAG, "createRfcommSocketToServiceRecord -> connect failed, error: "  + e.getMessage());
                onConnectionFailed(mBtDevice);
                try {
                    this.mBtSocket.close();
                } catch (IOException e2) {
                    PLog.e(BTConnectManager.TAG, "close() fail", e2);
                }
                BTConnectManager.this.scheduleAcceptWait();
            }

            PLog.d(BTConnectManager.TAG, "End bt connectBluetooth thread");
        }

        public void cancel() {
            try {
                if (mBtSocket != null) {
                    mBtSocket.close();
                }
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "Close bt connectBluetooth socket failed", e);
            }
        }
    }

    public BluetoothDevice getConnectedDevice() {
        return mConnectedDevice;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mBtSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private boolean isCanceled = false;

        public ConnectedThread(BluetoothSocket socket) {
            PLog.d(BTConnectManager.TAG, "Construct ConnectedThread");
            this.mBtSocket = socket;
            InputStream is = null;
            OutputStream os = null;
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "Get Stream failed", e);
            }
            this.mInputStream = is;
            this.mOutputStream = os;
        }

        public void run() {
            PLog.e(BTConnectManager.TAG, "Begin mConnectedThread");
            byte[] buffer = new byte[1024];
            while (!isCanceled) {
                try {
                    int bytes = this.mInputStream.read(buffer);
                    if (bytes != -1) {
                        notifyReceiveData(mBtSocket== null ? null : mBtSocket.getRemoteDevice(), bytes, buffer);
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e2) {
                    PLog.e(BTConnectManager.TAG, "connection break", e2);
                    BTConnectManager.this.onConnectionBreak(mBtSocket == null ? null : mBtSocket.getRemoteDevice());
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                PLog.e("write: "+ new String(buffer));
                this.mOutputStream.write(buffer);
                this.mOutputStream.flush();
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            isCanceled = true;
            PLog.e(BTConnectManager.TAG, "Cancel " + this);
            try {
                if (mBtSocket != null) {
                    mBtSocket.close();
                }
            } catch (IOException e) {
                PLog.e(BTConnectManager.TAG, "Close bt onConnected socket failed", e);
            }
        }
    }

    private synchronized void setState(int state) {
        this.mBtState = state;
    }

    public synchronized int getState() {
        return this.mBtState;
    }

    //开启等待连接
    public synchronized void scheduleAcceptWait() {
//        PLog.e(TAG, "进入acceptWait");
        if (this.mAcceptThread == null && this.mConnectedThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(STATE_ACCEPT_WAIT);
    }

    public synchronized void connectBluetooth(String address) {
        PLog.e("cd: "+address); //connect address
        if (TextUtils.isEmpty(address)){
            return;
        }

        connectBluetooth(mAdapter.getRemoteDevice(address));
    }

    public synchronized void connectBluetooth(BluetoothDevice device) {
        if (!mAdapter.isEnabled()){
            ToastUtils.showShort("蓝牙未开启！");
            PLog.e("connectBluetooth => 蓝牙未开启");
            return;
        }

        cancelAllBtThread();
        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private synchronized void onConnected(BluetoothSocket socket, BluetoothDevice device) {
        if (!Utils.isBtValid(device.getAddress())){
            if (ActivityUtils.getTopActivity() != null){
                ActivityUtils.getTopActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(ActivityUtils.getTopActivity())
                                        .setTitle(R.string.warn)
                                        .setMessage(R.string.not_support_device_tip)
                                        .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        }
                );
            }
            return;
        }

        mConnectedDevice = device;
        cancelAllBtThread();
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        InkConfig.setLastConnectedBt(device.getAddress());
        setState(STATE_CONNECTED);

        notifyConnectionSuccess(device);
    }

    public synchronized void cancelAllBtThread() {
//        PLog.e(TAG, "cancelAllBtThread方法");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public synchronized void disconnectBluetooth(){
        if (!isConnected()){
            return;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
    }

    public void destroy(){
        mListeners.clear();
        cancelAllBtThread();
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.mBtState != STATE_CONNECTED) {
                return;
            }
            ConnectedThread r = this.mConnectedThread;
            r.write(out);
        }
    }

    private void onConnectionFailed(BluetoothDevice device) {
        setState(STATE_ACCEPT_WAIT);
        this.mConnectedThread = null;
        mConnectedDevice = null;
        scheduleAcceptWait();
        notifyConnectionFailure(device);
    }

    private void onConnectionBreak(BluetoothDevice device) {
        setState(STATE_ACCEPT_WAIT);
        this.mConnectedThread = null;
        mConnectedDevice = null;
        scheduleAcceptWait();
        notifyConnectionBreak(device);
    }

    private void notifyConnectionFailure(BluetoothDevice device){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnBtConnectListener btConnectListener : mListeners){
                    btConnectListener.onConnectionFailure(device);
                }
            }
        });
    }

    private void notifyConnectionBreak(BluetoothDevice device){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnBtConnectListener btConnectListener : mListeners){
                    btConnectListener.onConnectionBreak(device);
                }
            }
        });
    }

    private void notifyConnectionSuccess(BluetoothDevice device){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnBtConnectListener btConnectListener : mListeners){
                    btConnectListener.onConnectionSuccess(device);
                }
            }
        });
    }

    private void notifyReceiveData(BluetoothDevice device, int bytes, byte[] data){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnBtConnectListener btConnectListener : mListeners){
                    btConnectListener.onReceiveData(device, bytes, data);
                }
            }
        });
    }

    public void addListener(OnBtConnectListener listener){
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(OnBtConnectListener listener){
        mListeners.remove(listener);
    }

    public interface OnBtConnectListener{
        void onConnectionFailure(BluetoothDevice device);

        void onConnectionBreak(BluetoothDevice device);

        void onConnectionSuccess(BluetoothDevice device);

        void onReceiveData(BluetoothDevice device, int bytes, byte[] data);
    }
}
