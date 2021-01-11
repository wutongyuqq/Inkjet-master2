package me.samlss.inkjet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import me.samlss.ebs.scanner.ScannerManager;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.LocationManager;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.utils.UmengUtils;
import me.samlss.ui.arch.QMUIFragmentActivity;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 主页
 */
public class MainActivity extends QMUIFragmentActivity {
    private long lastBackTime;
    @Override
    public int getContextViewId() {
        return R.id.main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            MainFragment fragment = new MainFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(getContextViewId(), fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }

        if (ScannerManager.getInstance().isSupportScanner()
                && !InkConfig.isNoMoreTipForScanner()){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.tip)
                    .setMessage(R.string.support_scanner)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                    .setNegativeButton(R.string.no_more_tip, (dialog, which) -> {
                        InkConfig.setNoMoreTipForScanner(true);
                        dialog.dismiss();
                    })
                    .show();
        }
    }
    @Override
    public void onBackPressed() {
        if (getCurrentFragment() instanceof MainFragment == false){
            super.onBackPressed();
            return;
        }

        long current = System.currentTimeMillis();
        if (lastBackTime == 0){
            lastBackTime = current;
            ToastUtils.showShort(R.string.press_again_to_exit);
            return;
        }

        //2 seconds
        if ((current - lastBackTime) < 1000 * 2){
            this.finish();
        }else{
            lastBackTime = current;
            ToastUtils.showShort(R.string.press_again_to_exit);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getCurrentFragment() != null){
            getCurrentFragment().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFragment() != null && getCurrentFragment() instanceof BaseFragment){
            ((BaseFragment)getCurrentFragment()).dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTConnectManager.getInstance().destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UmengUtils.doResume(this);
        ScannerManager.getInstance().start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        UmengUtils.doPause(this);
        LocationManager.get().stopListening();

        if (isFinishing()){
            LocationManager.get().destroy();
        }

        ScannerManager.getInstance().stop();
    }
}
