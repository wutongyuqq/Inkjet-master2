package me.samlss.inkjet.ui.fragments;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.cesgroup.numpickerview.NumberPickerView;
import me.samlss.ebs.EBSClient;
import me.samlss.ebs.EBSControlParameter;
import me.samlss.ebs.EBSRequestCallback;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.WifiManager;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码设置
 */
public class PrintParamFragment extends BaseFragment {
    @BindView(R.id.np_spot_size)
    NumberPickerView mNpSpotSize;

    @BindView(R.id.np_resolution)
    NumberPickerView mNpResolution;

    @BindView(R.id.np_repeat)
    NumberPickerView mNpRepeat;

    @BindView(R.id.np_interval)
    NumberPickerView mNpInterval;

    @BindView(R.id.np_distance)
    NumberPickerView mNpDistance;

    @BindView(R.id.np_pressure)
    NumberPickerView mNpPressure;

    @BindView(R.id.acs_direction)
    Spinner mSpDirection;

    @BindView(R.id.tb_horizontal_flip)
    SwitchCompat mScHorizontalFlip;

    @BindView(R.id.tb_vertical_flip)
    SwitchCompat mScVerticalFlip;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_print_parameter, null);
        ButterKnife.bind(this, layout);
        ViewUtils.setBackground(layout.findViewById(R.id.btn_confirm),
                DrawableUtils.getRectDrawable(
                        ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        initViews();
        return layout;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void initViews(){
        mNpSpotSize.setMaxValue(20) //最大输入值，也就是限量，默认无限大
                .setMinDefaultNum(1)  // 最小限定量
                .setCurrentNum(InkConfig.getSpotSize())  // 当前数量
                .setmOnClickInputListener(new NumberPickerView.OnClickInputListener() {
                    @Override
                    public void onChange(int value) {
                        InkConfig.setSpotSize(value);
                    }
                });

        mNpResolution.setMaxValue(2000) //最大输入值，也就是限量，默认无限大
                .setMinDefaultNum(100)  // 最小限定量
                .setDeltaValue(50)
                .setCurrentNum(InkConfig.getResolution())  // 当前数量
                .setmOnClickInputListener(new NumberPickerView.OnClickInputListener() {
                    @Override
                    public void onChange(int value) {
                        InkConfig.setResolution(value);
                    }
                });

        mNpRepeat.setMaxValue(20) //最大输入值，也就是限量，默认无限大
                .setMinDefaultNum(1)  // 最小限定量
                .setCurrentNum(InkConfig.getRepeat())  // 当前数量
                .setmOnClickInputListener(new NumberPickerView.OnClickInputListener() {
                    @Override
                    public void onChange(int value) {
                        InkConfig.setRepeat(value);
                    }
                });

        mNpInterval.setMaxValue(1000) //最大输入值，也就是限量，默认无限大
                .setMinDefaultNum(0)  // 最小限定量
                .setDeltaValue(50)
                .setCurrentNum(InkConfig.getInterval())  // 当前数量
                .setmOnClickInputListener(new NumberPickerView.OnClickInputListener() {
                    @Override
                    public void onChange(int value) {
                        InkConfig.setInterval(value);
                    }
                });

        mNpDistance.setMaxValue(1000) //最大输入值，也就是限量，默认无限大
                .setMinDefaultNum(0)  // 最小限定量
                .setDeltaValue(50)
                .setCurrentNum(InkConfig.getDistance())  // 当前数量
                .setmOnClickInputListener(new NumberPickerView.OnClickInputListener() {
                    @Override
                    public void onChange(int value) {
                        InkConfig.setDistance(value);
                    }
                });

        mNpPressure.setMaxValue(45) //最大输入值，也就是限量，默认无限大
                .setMinDefaultNum(15)  // 最小限定量
                .setDeltaValue(1)
                .setCurrentNum(InkConfig.getPressure())  // 当前数量
                .setmOnClickInputListener(new NumberPickerView.OnClickInputListener() {
                    @Override
                    public void onChange(int value) {
                        InkConfig.setPressure(value);
                    }
                });

        mSpDirection.setSelection(InkConfig.getDirection());
        mSpDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                InkConfig.setDirection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mScHorizontalFlip.setChecked(InkConfig.getHorizontalFlip());
        mScHorizontalFlip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                InkConfig.setHorizontalFlip(isChecked);
            }
        });
        mScVerticalFlip.setChecked(InkConfig.getVerticalFlip());
        mScVerticalFlip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                InkConfig.setVerticalFlip(isChecked);
            }
        });
    }

    private void saveSettings() {
        if (!WifiManager.getInstance().isConnected()){
            ToastUtils.showShort("Wifi尚未连接，无法保存参数设置！");
            return;
        }

        QMUITipDialog saving = new QMUITipDialog.Builder(getActivity())
                .setTipWord("正在保存...")
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();

        EBSClient.getInstance().requestAsync(new EBSControlParameter()
                        .setDirection(InkConfig.getDirection())
                        .setDistance(InkConfig.getDistance())
                        .setInterval(InkConfig.getInterval())
                        .setHorizontalFlip(InkConfig.getHorizontalFlip())
                        .setVerticalFlip(InkConfig.getVerticalFlip())
                        .setSpotSize(InkConfig.getSpotSize())
                        .setPressure(InkConfig.getPressure())
                        .setRepeat(InkConfig.getRepeat())
                        .setResolution(InkConfig.getResolution()), new EBSRequestCallback() {
                    @Override
                    public void onDone(String response) {
                        //{"Status":"OK"}
                        if (!TextUtils.isEmpty(response)){
                            try{
                                JSONObject jsonObject = JSON.parseObject(response);
                                String status = jsonObject.getString("Status");
                                if (!TextUtils.isEmpty(status) && status.equalsIgnoreCase("OK")){
                                    ToastUtils.showShort("保存成功~");
                                }else{
                                    ToastUtils.showShort("保存失败！");
                                }
                            }catch (Exception e){
                                ToastUtils.showShort("保存失败！");
                                e.printStackTrace();
                            }
                        }else{
                            ToastUtils.showShort("保存失败！");
                        }
                    }
                });
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_confirm:
                saveSettings();
                break;
        }
    }
}
