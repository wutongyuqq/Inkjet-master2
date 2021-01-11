package me.samlss.inkjet.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ScreenUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 企业注册码输入框
 */
public class CheckCompanyCodeDialog extends Dialog {
    @BindView(R.id.edit_text)
    EditText mEditText;

    @BindView(R.id.btn_activate)
    Button mBtnActivate;

    private OnButtonActivateClickListener mOnButtonActivateClickListener;

    public CheckCompanyCodeDialog(@NonNull Context context) {
        super(context, R.style.PopupDialog);

        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_company_register_code);
        ButterKnife.bind(this);
        ViewUtils.setBackground(mBtnActivate,
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green),
                        DensityUtils.dp2px(5)));
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (ScreenUtils.getScreenWidth() * 0.8f);
        getWindow().setAttributes(p);

        mEditText.requestFocus();
        KeyboardUtils.showSoftInput(mEditText);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        KeyboardUtils.hideSoftInput(mEditText);
    }

    @OnClick({R.id.btn_activate})
    public void onClick(View view){
        if (mOnButtonActivateClickListener != null){
            mOnButtonActivateClickListener.onClick(view, mEditText.getText().toString());
        }
    }

    public void setButtonActivateClickListener(OnButtonActivateClickListener buttonActivateClickListener) {
        this.mOnButtonActivateClickListener = buttonActivateClickListener;
    }

    public interface OnButtonActivateClickListener{
        void onClick(View view, String text);
    }
}
