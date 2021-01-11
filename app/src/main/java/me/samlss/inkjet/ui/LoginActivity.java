package me.samlss.inkjet.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ActivityUtils;
import me.samlss.framework.utils.CacheMemoryUtils;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.NetworkUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.base.BaseActivity;
import me.samlss.inkjet.ui.model.LoginModel;
import me.samlss.inkjet.utils.ImageUtils;
import me.samlss.inkjet.utils.Utils;
import me.samlss.ui.util.QMUIStatusBarHelper;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description login activity
 */
public class LoginActivity extends BaseActivity {
    private Button mBtnLogin;
    private ImageView mIvPswClear;
    private boolean isShowPasswordClear;

    private ImageView mIvCompanyLogo;
    private TextView mTvCompanyName;
    private EditText mEtAccount;
    private EditText mEtPsw;

    private QMUITipDialog mLoadingDialog;
    private LoginModel mLoginModel;
    private String mCompanyCode;

    private ImageUtils.ImageTask mImageTask;

    EditText mEtCompanyCode;
    Button mBtnActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginModel = new LoginModel(this);

        if (TextUtils.isEmpty(InkConfig.getCacheCompanyHost()) || hasExpired()){
            if (hasExpired()){
                showExpiredDialog();
            }
            initVerifyCompanyCode();
        }else{
            checkAndLaunchWhat();
        }
    }

    private void showExpiredDialog(){
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("该企业已过有效期，详情请联系商务！")
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private boolean hasExpired(){
        try{
            String expired = InkConfig.getCompanyExpired();
            if (!TextUtils.isEmpty(expired)){
                long expiredTime = Long.valueOf(expired);
                long nowTime = System.currentTimeMillis() / 1000;
                return nowTime > expiredTime;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mLoginModel.destroy();
        if (mImageTask != null && !mImageTask.isCanceled()){
            mImageTask.cancel();
        }

        if (!InkConfig.getCacheCompanyLogoUrl().equals(InkConfig.getCacheCompanyBannerUrl())) {
            ImageUtils.recycleBitmap(CacheMemoryUtils.getInstance().get(InkConfig.getCacheCompanyLogoUrl()));
            CacheMemoryUtils.getInstance().remove(InkConfig.getCacheCompanyLogoUrl());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mEtCompanyCode != null) {
            KeyboardUtils.hideKeyboardWhenTouchViewOutSide(this, ev, mEtCompanyCode);
        }

        if (mEtPsw != null) {
            KeyboardUtils.hideKeyboardWhenTouchViewOutSide(this, ev, mEtPsw);
        }

        if (mEtAccount != null) {
            KeyboardUtils.hideKeyboardWhenTouchViewOutSide(this, ev, mEtAccount);
        }

        return super.dispatchTouchEvent(ev);
    }

    private void initVerifyCompanyCode(){
        mLoadingDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.verifying))
                .create();

        mLoadingDialog.setCanceledOnTouchOutside(false);

        setContentView(R.layout.layout_check_company_code);
        QMUIStatusBarHelper.setStatusBarLightMode(this);

        mEtCompanyCode = findViewById(R.id.edit_text);
        mEtCompanyCode.setText(InkConfig.getCacheCompanyCode());
        mEtCompanyCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onCheckCompanyCode(mEtCompanyCode.getText().toString());
            }
            return (actionId == EditorInfo.IME_ACTION_DONE);
        });
        mBtnActivate = findViewById(R.id.btn_activate);
        mBtnActivate.setOnClickListener(v -> onCheckCompanyCode(mEtCompanyCode.getText().toString()));

        ViewUtils.setBackground(mBtnActivate,
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green),
                        DensityUtils.dp2px(5)));
    }

    private void initialLogin(){
        setContentView(R.layout.activity_login);
        QMUIStatusBarHelper.setStatusBarDarkMode(this);

        mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(mClickListener);

        mIvCompanyLogo = findViewById(R.id.iv_company_logo);
        mTvCompanyName = findViewById(R.id.tv_company_name);

        mIvPswClear = findViewById(R.id.iv_toggle_psw);
        mIvPswClear.setOnClickListener(mClickListener);

        mEtAccount = findViewById(R.id.edit_account);
        mEtPsw = findViewById(R.id.edit_password);

        mEtPsw.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mEtPsw.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId== EditorInfo.IME_ACTION_DONE||
                    (event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {
                onLogin(mEtAccount.getText().toString(), mEtPsw.getText().toString());
                return true;
            }

            return false;
        });

        ViewUtils.setBackground(mBtnLogin,
                DrawableUtils.getRectDrawable(
                        ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        mLoadingDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.logging))
                .create();

        mLoadingDialog.setCanceledOnTouchOutside(false);

        boolean autoLogin = InkConfig.getCacheAccount() != null && InkConfig.getCachePassword() != null;

        if (InkConfig.getCacheAccount() != null) {
            mEtAccount.setText(InkConfig.getCacheAccount());
        }
        if (InkConfig.getCachePassword() != null) {
            mEtPsw.setText(InkConfig.getCachePassword());
        }

        showCompanyInfo();
        if (autoLogin && InkConfig.isAutoLogin()) {
            onLogin(InkConfig.getCacheAccount(), InkConfig.getCachePassword());
        }

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReVerifyCompanyCode();
            }
        });
    }

    private void onReVerifyCompanyCode(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.tip)
                .setMessage(R.string.verify_company_code_or_not_not_login)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    Utils.changeCompanyCode();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 显示公司信息
     * */
    private void showCompanyInfo(){
        mTvCompanyName.setText(InkConfig.getCacheCompanyName());
        mImageTask = ImageUtils.showImage(InkConfig.getCacheCompanyLogoUrl(), mIvCompanyLogo);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_login:
                    onLogin(mEtAccount.getText().toString(), mEtPsw.getText().toString());
                    break;

                case R.id.iv_toggle_psw:
                    onTogglePsw();
                    break;

                default: break;
            }
        }
    };

    private void onCheckCompanyCode(String code){
        mCompanyCode = code;
        if (TextUtils.isEmpty(code)
            /*|| code.length() != 16*/){
            ToastUtils.showShort(R.string.input_company_code);
            return;
        }

        if (!NetworkUtils.isConnected()){
            ToastUtils.showShort(R.string.no_net);
            return;
        }

        KeyboardUtils.hideSoftInput(mEtCompanyCode);
        mLoadingDialog.show();
        mLoginModel.checkCompanyCode(code);
    }

    public void onCheckCompanyCodeFailure(String msg){
        if (msg == null){
            msg = ResourceUtils.getString(R.string.check_company_code_failed);
        }

        ToastUtils.showShort(msg);
        mLoadingDialog.dismiss();
    }

    private void checkAndLaunchWhat(){
        if (InkConfig.getCompanyMode() == 0) {
            initialLogin();
        }else{
            UserManager.getInstance().setUserId("none");
            launchMain();
        }
    }

    public void onCheckCompanyCodeSuccessButExpired(){
        showExpiredDialog();
    }

    public void onCheckCompanyCodeSuccess(String companyHost, String companyName, String companyLogoUrl, String bannerUrl, String companyQrCodeUrl){
        InkConfig.setCacheCompanyHost(companyHost);
        InkConfig.setCacheCompanyName(companyName);
        InkConfig.setCacheCompanyLogoUrl(companyLogoUrl);
        InkConfig.setCacheCompanyBannerUrl(bannerUrl);
        InkConfig.setCompanyQRCodeUrl(companyQrCodeUrl);
        InkConfig.setCacheCompanyCode(mCompanyCode);

        ImageUtils.showImage(bannerUrl, new ImageView(getApplicationContext()));
        mLoadingDialog.dismiss();

        checkAndLaunchWhat();
    }

    private void onLogin(String userName, String password){
        if (TextUtils.isEmpty(userName)){
            ToastUtils.showShort(R.string.input_valid_account);
            return;
        }

        if (TextUtils.isEmpty(password)){
            ToastUtils.showShort(R.string.input_password);
            return;
        }

        if (!NetworkUtils.isConnected()){
            ToastUtils.showShort(R.string.no_net);
            return;
        }
        KeyboardUtils.hideSoftInput(mEtPsw);
        mLoadingDialog.show();
        mLoginModel.login(userName, password, InkConfig.getCacheCompanyCode());
    }

    public void onLoginFailure(String msg){
        if (msg == null){
            msg = ResourceUtils.getString(R.string.login_failed);
        }

        mLoadingDialog.dismiss();
        ToastUtils.showShort(msg);
    }

    public void onLoginSuccess(){
        mLoadingDialog.dismiss();
        InkConfig.setAutoLogin(true);
        InkConfig.setCacheAccount(mEtAccount.getText().toString());
        InkConfig.setCachePassword(mEtPsw.getText().toString());

        launchMain();
    }

    private void launchMain(){
        ActivityUtils.startActivity(new Intent(getBaseContext(), MainActivity.class));
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        finish();
    }

    private void onTogglePsw(){
        isShowPasswordClear = !isShowPasswordClear;
        mIvPswClear.setImageResource(isShowPasswordClear ? R.mipmap.icon_edit_psw_show : R.mipmap.icon_edit_psw_hide);
        mEtPsw.setTransformationMethod(isShowPasswordClear ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
    }
}
