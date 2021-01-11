package me.samlss.inkjet.ui.data;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.base.ItemDescription;
import me.samlss.inkjet.ui.fragments.ExcelInkjetFragment;
import me.samlss.inkjet.ui.fragments.FakeManualInputNumberFragment;
import me.samlss.inkjet.ui.fragments.InkAnnalFragment;
import me.samlss.inkjet.ui.fragments.ManualInkFragment;
import me.samlss.inkjet.ui.fragments.PrintSettingsFragment;
import me.samlss.inkjet.ui.fragments.ProjectFragment;
import me.samlss.inkjet.ui.fragments.ScanQRCodeFragment;
import me.samlss.inkjet.ui.fragments.SettingsFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description items creator
 */
public class ItemContainer {
    private static ItemContainer sInstance = new ItemContainer();
    private Map<Class<? extends BaseFragment>, ItemDescription> mWidgets;

    private ItemContainer() {
        mWidgets = new HashMap<>();
        mWidgets.put(ExcelInkjetFragment.class, new ItemDescription(ExcelInkjetFragment.class, ResourceUtils.getString(R.string.excel_code), R.drawable.ic_edit_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#3CB7F1"))));
        mWidgets.put(ManualInkFragment.class, new ItemDescription(ManualInkFragment.class, ResourceUtils.getString(R.string.manual_code), R.drawable.ic_manual_code_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#EB5887"))));
        //mWidgets.put(ScanQRCodeFragment.class, new ItemDescription(ScanQRCodeFragment.class, ResourceUtils.getString(R.string.scan_qr_code_to_get_excel), R.drawable.ic_scan_qrcode_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#F44336"))));
        //mWidgets.put(FakeManualInputNumberFragment.class, new ItemDescription(FakeManualInputNumberFragment.class, ResourceUtils.getString(R.string.manual_pan_number), R.drawable.ic_manual_number_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#9575CD"))));
        mWidgets.put(ProjectFragment.class, new ItemDescription(ProjectFragment.class, ResourceUtils.getString(R.string.project_manager), R.drawable.ic_project_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#81C784"))));
        mWidgets.put(InkAnnalFragment.class, new ItemDescription(InkAnnalFragment.class, ResourceUtils.getString(R.string.code_record), R.drawable.ic_code_record_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#45B1D1"))));
        mWidgets.put(SettingsFragment.class, new ItemDescription(SettingsFragment.class, ResourceUtils.getString(R.string.setting), R.drawable.ic_setting_white_128, DrawableUtils.getOvalDrawable(Color.parseColor("#426D9A"))));
        mWidgets.put(PrintSettingsFragment.class, new ItemDescription(PrintSettingsFragment.class, ResourceUtils.getString(R.string.device_setting), R.drawable.ic_device_setting_white_128dp, DrawableUtils.getOvalDrawable(Color.parseColor("#56BEA5"))));
    }

    public static ItemContainer getInstance() {
        return sInstance;
    }

    public ItemDescription get(Class<? extends BaseFragment> fragment) {
        return mWidgets.get(fragment);
    }
}
