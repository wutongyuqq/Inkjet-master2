package me.samlss.inkjet.ui.fragments;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.ebs.EBSClient;
import me.samlss.ebs.EBSOpParameter;
import me.samlss.ebs.EBSRequestCallback;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.NetworkUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.WifiManager;
import me.samlss.inkjet.ui.adapters.FontSectionEntity;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.dialogs.FontDialog;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码设置
 */
public class PrintSettingsFragment extends BaseFragment {
    @BindView(R.id.btn_ip_test)
    TextView mBtnIpTest;

    @BindView(R.id.edit_wifi_ip)
    EditText mEtWifiIp;

    @BindView(R.id.tv_bt_state)
    TextView mTvBluetoothState;

    @BindView(R.id.tv_wifi_state)
    TextView mTvWifiState;

    @BindView(R.id.tb_auto_wifi)
    SwitchCompat mAutoWifiCompat;

    @BindView(R.id.tv_font)
    TextView mTvFont;

    @BindView(R.id.et_font_size)
    EditText mEtFontSize;

    @BindView(R.id.layout_font_size)
    View mLayoutFontSize;

    @BindView(R.id.view_font_size_bottom_line)
    View mFontLine;

    @BindView(R.id.switch_continuous_print)
    SwitchCompat mSwitchContinuousPrint;

    private FontDialog mFontDialog;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_print_settings, null);
        ButterKnife.bind(this, layout);

        ViewUtils.setBackground(mBtnIpTest,
                DrawableUtils.getRectDrawable(
                        ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));
        initializeBanner(layout.findViewById(R.id.iv_header));
        initializeEtWifiIp();
        initState();

        mAutoWifiCompat.setChecked(InkConfig.isAutoWifi());
        mAutoWifiCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                InkConfig.setAutoWifi(isChecked);
            }
        });

        mTvFont.setText(InkConfig.getDisplayFont());
        updateFontType();
        mEtFontSize.setText(String.valueOf(InkConfig.getFontSize()));
        mEtFontSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    InkConfig.setFontSize(Integer.valueOf(mEtFontSize.getText().toString()));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSwitchContinuousPrint.setChecked(InkConfig.getContinuousPrint());

        mSwitchContinuousPrint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                InkConfig.setContinuousPrint(isChecked);
            }
        });
        return layout;
    }

    private void initializeEtWifiIp(){
        mEtWifiIp.setText(InkConfig.getWifiIp());
        mEtWifiIp.addTextChangedListener(mEtWifiIpTextChange);
    }

    private void initState(){
        mTvBluetoothState.setText(BTConnectManager.getInstance().isConnected() ? "蓝牙已连接" : "蓝牙未连接");
        mTvWifiState.setText(WifiManager.getInstance().isConnected() ? "Wifi已连接" : "Wifi未连接");
    }

    private TextWatcher mEtWifiIpTextChange = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            InkConfig.setWifiIp(mEtWifiIp.getText().toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtWifiIp);
        return super.dispatchTouchEvent(ev);
    }

    private void testWifiIp() {
//        if (!NetworkUtils.isConnected()){
//            ToastUtils.showShort(R.string.no_net);
//            return;
//        }

        String input = mEtWifiIp.getText().toString();
        if (TextUtils.isEmpty(input)){
            ToastUtils.showShort("请输入喷码机端Wifi的IP地址！");
            return;
        }

        final QMUITipDialog loading = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在连接...")
                .create();
        loading.show();

        WifiManager.getInstance().connect(new WifiManager.ConnectCallback() {
            @Override
            public void onConnect(boolean isConnected) {
                loading.dismiss();
                if (isConnected){
                    ToastUtils.showShort("Wifi连接成功~");
                }else{
                    ToastUtils.showShort("Wifi连接失败！");
                }
                initState();
            }
        });
    }

    private void updateFontType(){
        if (InkConfig.getFontType() == FontSectionEntity.TYPE_INTERNAL){
            mLayoutFontSize.setVisibility(View.GONE);
            mFontLine.setVisibility(View.GONE);
        }   else{
            mLayoutFontSize.setVisibility(View.VISIBLE);
            mFontLine.setVisibility(View.VISIBLE);
        }
    }

    private void showFontDialog() {
        if (mFontDialog == null){
            mFontDialog = new FontDialog(getActivity());
            mFontDialog.setOnItemSelectedListener(new FontDialog.OnItemSelectedListener() {
                @Override
                public void onItemSelect(int position, int type, String displayFont, String paramFont) {
                    PLog.e("displayFont: "+displayFont + ", paramFont: "+paramFont);

                    InkConfig.setFontType(type);
                    InkConfig.setFontParam(paramFont);
                    InkConfig.setDisplayFont(displayFont);
                    mTvFont.setText(displayFont);
                    mFontDialog.dismiss();

                    updateFontType();
                }
            });
        }

        mFontDialog.show();
    }

    @OnClick({R.id.btn_ip_test, R.id.layout_print_setting_des, R.id.layout_print_param, R.id.layout_set_font/*, R.id.layout_reboot, R.id.layout_shut_down*/})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_ip_test:
                testWifiIp();
                break;

            case R.id.layout_print_setting_des:
                startFragment(new PrintSettingsDesFragment());
                break;

            case R.id.layout_print_param:
                startFragment(new PrintParamFragment());
                break;

            case R.id.layout_set_font:
                showFontDialog();
                break;
        }
    }
}
