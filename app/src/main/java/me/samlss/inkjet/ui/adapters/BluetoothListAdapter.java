package me.samlss.inkjet.ui.adapters;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.utils.BluetoothUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 蓝牙列表适配器
 */
public class BluetoothListAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {


    public BluetoothListAdapter(int layoutResId, @Nullable List<BluetoothDevice> data) {
        super(layoutResId, data);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {
        helper.setText(R.id.tv_name, BluetoothUtils.getDisplayName(item));
        helper.setImageDrawable(R.id.iv_type, getTypeDrawable(item));
    }


    /**
     * {@link BluetoothClass#getMajorDeviceClass()}
     * */
    private Drawable getTypeDrawable(BluetoothDevice bluetoothDevice){
        if (bluetoothDevice == null
                || bluetoothDevice.getBluetoothClass() == null){
            return ResourceUtils.getDrawable(R.drawable.ic_bluetooth_white_72dp);
        }

        switch (bluetoothDevice.getBluetoothClass().getMajorDeviceClass()){
            case BluetoothClass.Device.Major.MISC:
                return ResourceUtils.getDrawable(R.drawable.ic_misc_white_72dp);

            case BluetoothClass.Device.Major.COMPUTER:
                return ResourceUtils.getDrawable(R.drawable.ic_computer_white_72dp);

            case BluetoothClass.Device.Major.PHONE:
                return ResourceUtils.getDrawable(R.drawable.ic_phone_white_72dp);

            case BluetoothClass.Device.Major.NETWORKING:
                return ResourceUtils.getDrawable(R.drawable.ic_networking_white_72dp);

            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return ResourceUtils.getDrawable(R.drawable.ic_bluetooth_white_72dp);

            case BluetoothClass.Device.Major.PERIPHERAL:
                return ResourceUtils.getDrawable(R.drawable.ic_peripheral_white_72dp);

            case BluetoothClass.Device.Major.IMAGING:
                return ResourceUtils.getDrawable(R.drawable.ic_bluetooth_white_72dp);

            case BluetoothClass.Device.Major.WEARABLE:
                return ResourceUtils.getDrawable(R.drawable.ic_wearable_white_72dp);

            case BluetoothClass.Device.Major.TOY:
                return ResourceUtils.getDrawable(R.drawable.ic_bluetooth_white_72dp);

            case BluetoothClass.Device.Major.HEALTH:
                return ResourceUtils.getDrawable(R.drawable.ic_health_white_72dp);
        }

        return ResourceUtils.getDrawable(R.drawable.ic_bluetooth_white_72dp);
    }

}
