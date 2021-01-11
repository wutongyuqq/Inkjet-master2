package me.samlss.inkjet.ui.dialogs;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import me.samlss.framework.permission.RequestExecutor;
import me.samlss.framework.utils.GotoUtils;
import me.samlss.inkjet.R;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class DialogUtils {
    private DialogUtils(){}

    /**
     * When this app request one permission or multiple permissions, but user have rejected it/them,
     * will show this dialog.
     *
     * @param activity Activity to attach
     * @param requestExecutor What you want to do if user reject permission.
     * */
    public static void showRejectedPermissionDialog(Activity activity, final RequestExecutor requestExecutor){
        if (activity == null){
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_tips)
                .setMessage(R.string.permission_rationale_tips)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (requestExecutor != null) {
                        requestExecutor.request();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (requestExecutor != null) {
                        requestExecutor.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void showRejectedPermissionDialog(Activity activity, final RequestExecutor requestExecutor, String message){
        if (activity == null){
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_tips)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (requestExecutor != null) {
                        requestExecutor.request();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (requestExecutor != null) {
                        requestExecutor.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * When user rejected permission(one or multiple) forever, will show this dialog
     * to notify the user to go to app settings to grant permissions manually.
     *
     * @param activity Activity to attach
     * */
    public static void showRejectedPermissionGotToAppSettingDialog(Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_tips)
                .setMessage(R.string.permission_forever_denied_tips)
                .setPositiveButton(R.string.confirm, (dialog, which) -> GotoUtils.openAppDetailSettings())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .create()
                .show();
    }


    /**
     * When user rejected permission(one or multiple) forever, will show this dialog
     * to notify the user to go to app settings to grant permissions manually.
     *
     * @param activity Activity to attach
     * */
    public static void showRejectedPermissionGotToAppSettingDialog(Activity activity, String message){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_tips)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> GotoUtils.openAppDetailSettings())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * When user rejected permission(one or multiple), will show this dialog
     * to notify the user that some functions are not available.
     * @param activity Activity to attach
     * */
    public static void showRejectedPermissionTip(Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.warn)
                .setMessage(R.string.rejected_permission_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .create()
                .show();
    }

    public static void showRejectedPermissionTip(Activity activity, String message){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.warn)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .create()
                .show();
    }
}
