package me.samlss.inkjet.ui.fragments;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.ui.widget.dialog.QMUIDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码设置
 */
public class PrintSettingsDesFragment extends BaseFragment {
    @BindView(R.id.sp_send_msg_format)
    Spinner mSpSendMsgFormat;

    @BindView(R.id.et_end_character)
    TextView mEtEndChar;

    @BindView(R.id.et_print_start)
    TextView mEtPrintStart;

    @BindView(R.id.et_print_finish)
    TextView mEtPrintFinish;

    @BindView(R.id.et_print_stop)
    TextView mEtPrintStop;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_print_settings_des, null);
        ButterKnife.bind(this, layout);

        ((TextView)layout.findViewById(R.id.tv_config_des)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        mEtEndChar.setText(String.valueOf(InkConfig.getEndCharacter()));
//        mEtEndChar.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String input = mEtEndChar.getText().toString();
//                try {
//                    int ascii = Integer.valueOf(input);
//                    if (ascii < 0 || ascii > 127){
//                        ToastUtils.showShort(R.string.input_valid_ascii);
//                    }else{
//                        InkConfig.setEndCharacter(ascii);
//                        ToastUtils.showShort(R.string.set_success);
//                        KeyboardUtils.hideSoftInput(mEtEndChar);
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                    ToastUtils.showShort(R.string.input_valid_ascii);
//                }
//            }
//            return (actionId == EditorInfo.IME_ACTION_DONE);
//        });

        mEtPrintStart.setText(String.valueOf(InkConfig.getPrintStartReturnValue()));
//        mEtPrintStart.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String input = mEtPrintStart.getText().toString();
//                try {
//                    InkConfig.setPrintStartReturnValue(input);
//                    ToastUtils.showShort(R.string.set_success);
//                    KeyboardUtils.hideSoftInput(mEtPrintStart);
//                }catch (Exception e){
//                    e.printStackTrace();
//                    ToastUtils.showShort(R.string.set_failed);
//                }
//            }
//            return (actionId == EditorInfo.IME_ACTION_DONE);
//        });

        mEtPrintFinish.setText(String.valueOf(InkConfig.getPrintFinishReturnValue()));
//        mEtPrintFinish.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String input = mEtPrintFinish.getText().toString();
//                try {
//                    InkConfig.setPrintFinishReturnValue(input);
//                    ToastUtils.showShort(R.string.set_success);
//                    KeyboardUtils.hideSoftInput(mEtPrintFinish);
//                }catch (Exception e){
//                    e.printStackTrace();
//                    ToastUtils.showShort(R.string.set_failed);
//                }
//            }
//            return (actionId == EditorInfo.IME_ACTION_DONE);
//        });

        mEtPrintStop.setText(String.valueOf(InkConfig.getPrintStopReturnValue()));
//        mEtPrintStop.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String input = mEtPrintStop.getText().toString();
//                try {
//                    InkConfig.setPrintStopReturnValue(input);
//                    ToastUtils.showShort(R.string.set_success);
//                    KeyboardUtils.hideSoftInput(mEtPrintStop);
//                }catch (Exception e){
//                    e.printStackTrace();
//                    ToastUtils.showShort(R.string.set_failed);
//                }
//            }
//            return (actionId == EditorInfo.IME_ACTION_DONE);
//        });
        initializeBanner(layout.findViewById(R.id.iv_header));
        return layout;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtEndChar);
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtPrintStart);
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtPrintFinish);
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtPrintStop);
        return super.dispatchTouchEvent(ev);
    }

    @OnClick({R.id.layout_description})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.layout_description:
                new QMUIDialog.MessageDialogBuilder(getActivity())
                        .setMessage(R.string.print_setting_description)
                        .show();
                break;
        }
    }
}
