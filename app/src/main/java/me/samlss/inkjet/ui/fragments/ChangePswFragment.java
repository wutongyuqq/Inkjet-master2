package me.samlss.inkjet.ui.fragments;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.ActivityUtils;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.NetworkUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.ui.LoginActivity;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.model.ChangePswModel;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 修改密码
 */
public class ChangePswFragment extends BaseFragment {
    @BindView(R.id.et_old_psw)
    EditText mEtOldPsw;

    @BindView(R.id.et_new_psw)
    EditText mEtNewPsw;

    @BindView(R.id.et_confirm_new_psw)
    EditText mEtConfirmNewPsw;

    private ChangePswModel mChangePswModel;
    private QMUITipDialog mLoadingTipDialog;

    private boolean isOldPswClear;
    private boolean isNewPswClear;
    private boolean isConfirmNewPswClear;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_change_psw, null);
        ButterKnife.bind(this, layout);

        ViewUtils.setBackground(layout.findViewById(R.id.btn_save),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        setAsteriskColorSpan(layout.findViewById(R.id.tv_old_psw), ResourceUtils.getString(R.string.old_psw));
        setAsteriskColorSpan(layout.findViewById(R.id.tv_new_psw), ResourceUtils.getString(R.string.new_psw));
        setAsteriskColorSpan(layout.findViewById(R.id.tv_confirm_new_psw), ResourceUtils.getString(R.string.confirm_new_psw));
        mEtOldPsw.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mEtNewPsw.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mEtConfirmNewPsw.setTransformationMethod(PasswordTransformationMethod.getInstance());

        mChangePswModel = new ChangePswModel(this);
        mLoadingTipDialog = new QMUITipDialog.Builder(getActivity())
                .setTipWord(ResourceUtils.getString(R.string.saving))
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mLoadingTipDialog.setCanceledOnTouchOutside(false);

        mEtConfirmNewPsw.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onChangePsw();
            }
            return (actionId == EditorInfo.IME_ACTION_DONE);
        });

        initializeBanner(layout.findViewById(R.id.iv_header));
        return layout;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtOldPsw);
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtNewPsw);
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtConfirmNewPsw);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChangePswModel.destroy();
        if (mLoadingTipDialog.isShowing()){
            mLoadingTipDialog.dismiss();
        }
    }

    private void setAsteriskColorSpan(TextView textView, String string){
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        SpannableString sp = new SpannableString(string);
        sp.setSpan(colorSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(sp);
    }

    private void onChangePsw(){
        if (!NetworkUtils.isConnected()){
            ToastUtils.showShort(R.string.no_net);
            return;
        }

        String oldPsw = mEtOldPsw.getText().toString();
        String newPsw = mEtNewPsw.getText().toString();
        String newPswAgain = mEtConfirmNewPsw.getText().toString();

        if (TextUtils.isEmpty(oldPsw)){
            ToastUtils.showShort(R.string.input_old_psw);
            return;
        }

        if (TextUtils.isEmpty(newPsw)){
            ToastUtils.showShort(R.string.input_new_psw);
            return;
        }

        if (TextUtils.isEmpty(newPswAgain)){
            ToastUtils.showShort(R.string.input_confirm_new_psw);
            return;
        }

        if (!newPsw.equalsIgnoreCase(newPswAgain)){
            ToastUtils.showShort(R.string.not_equal_new_psw);
            return;
        }

        mLoadingTipDialog.show();
        mChangePswModel.changePsw(oldPsw, newPsw);
    }

    public void onChangePswSuccess(){
        mLoadingTipDialog.dismiss();
//        UserManager.getInstance().setUserId(null);
        InkConfig.setCachePassword("");
        InkConfig.setAutoLogin(false);
        ActivityUtils.finishAllActivities();
        ActivityUtils.startActivity(LoginActivity.class);
    }

    public void onChangePswFailure(String msg){
        mLoadingTipDialog.dismiss();
        if (msg == null){
            msg = ResourceUtils.getString(R.string.change_psw_failed);
        }
        ToastUtils.showShort(msg);
    }

    private void setPswState(ImageView imageView, EditText editText, boolean isClear){
        imageView.setImageResource(isClear ? R.mipmap.icon_edit_psw_show : R.mipmap.icon_edit_psw_hide);
        editText.setTransformationMethod(isClear ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
    }

    @OnClick({R.id.btn_save, R.id.iv_toggle_old_psw, R.id.iv_toggle_new_psw, R.id.iv_toggle_confirm_new_psw})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_save:
                onChangePsw();
                break;

            case R.id.iv_toggle_old_psw:
                isOldPswClear = !isOldPswClear;
                setPswState((ImageView) view, mEtOldPsw, isOldPswClear);
                break;

            case R.id.iv_toggle_new_psw:
                isNewPswClear = !isNewPswClear;
                setPswState((ImageView) view, mEtNewPsw, isNewPswClear);
                break;

            case R.id.iv_toggle_confirm_new_psw:
                isConfirmNewPswClear = !isConfirmNewPswClear;
                setPswState((ImageView) view, mEtConfirmNewPsw, isConfirmNewPswClear);
                break;
        }
    }
}
