package me.samlss.inkjet.ui.permission;

import android.app.Activity;

import java.lang.ref.WeakReference;

import me.samlss.framework.permission.RationaleCallBack;
import me.samlss.framework.permission.RequestExecutor;
import me.samlss.inkjet.ui.dialogs.DialogUtils;

/**
 * @author SamLeung
 * @e-mail 729717222@qq.com
 * @github https://github.com/samlss
 * @csdn https://blog.csdn.net/Samlss
 * @description The rationale action of permission request.
 */
public class RuntimeRationale implements RationaleCallBack {
    private WeakReference<Activity> activityWeakReference;

    public RuntimeRationale(Activity activity){
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onRationale(String permission, RequestExecutor requestExecutor) {
        if (activityWeakReference == null
                || activityWeakReference.get() == null){
            return;
        }

        DialogUtils.showRejectedPermissionDialog(activityWeakReference.get(), requestExecutor);
    }
}
