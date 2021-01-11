package me.samlss.inkjet.utils;

import android.bluetooth.BluetoothDevice;

import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 蓝牙工具类
 */
public class BluetoothUtils {
    private BluetoothUtils(){}

    public static String getDisplayName(BluetoothDevice bluetoothDevice){
        if (bluetoothDevice == null){
            return ResourceUtils.getString(R.string.unknown_device);
        }

        if (bluetoothDevice.getName() != null){
            return bluetoothDevice.getName();
        }

        if (bluetoothDevice.getAddress() != null){
            return bluetoothDevice.getAddress();
        }

        return ResourceUtils.getString(R.string.unknown_device);
    }
}
