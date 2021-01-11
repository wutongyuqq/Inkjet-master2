package me.samlss.inkjet.app;

import android.app.Application;
import android.view.Gravity;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.tencent.smtt.sdk.QbSdk;

import me.samlss.ebs.EBSClient;
import me.samlss.ebs.scanner.ScannerManager;
import me.samlss.framework.log.LogConfig;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.BuildConfig;
import me.samlss.inkjet.api.Api;
import me.samlss.inkjet.crash.CrashHandler;
import me.samlss.inkjet.managers.WifiManager;
import me.samlss.inkjet.utils.UmengUtils;
import me.samlss.ui.arch.QMUISwipeBackActivityManager;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 应用主入口
 */
public class InkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize(){
        LogConfig.sDEBUG = BuildConfig.DEBUG;
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        MMKV.initialize(this);
        EBSClient.initialize(this);
        WifiManager.getInstance().connect(null);
        QMUISwipeBackActivityManager.init(this);
        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                PLog.e("onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                PLog.e("onViewInitFinished: "+b);
            }
        });
        Api.initialize();
        UmengUtils.initialize(this, BuildConfig.FLAVOR);
        ScannerManager.initialize(this);
        CrashHandler.getInstance().init(this);
        CrashReport.initCrashReport(getApplicationContext(), "f6d3c18d57", false);
    }
}
