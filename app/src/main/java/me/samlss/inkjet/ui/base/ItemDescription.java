package me.samlss.inkjet.ui.base;

import android.graphics.drawable.Drawable;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 设置
 */
public class ItemDescription {
    private Class<? extends BaseFragment> mKitDemoClass;
    private String mKitName;
    private int mIconRes;
    private String mDocUrl;
    private Drawable mBgDrawable;

    public ItemDescription(Class<? extends BaseFragment> kitDemoClass, String kitName){
        this(kitDemoClass, kitName, 0, "");
    }

    public ItemDescription(Class<? extends BaseFragment> mKitDemoClass, String mKitName, int mIconRes, Drawable bg) {
        this.mKitDemoClass = mKitDemoClass;
        this.mKitName = mKitName;
        this.mIconRes = mIconRes;
        this.mBgDrawable = bg;
    }

    public ItemDescription(Class<? extends BaseFragment> kitDemoClass, String kitName, int iconRes, String docUrl) {
        mKitDemoClass = kitDemoClass;
        mKitName = kitName;
        mIconRes = iconRes;
        mDocUrl = docUrl;
    }

    public Class<? extends BaseFragment> getDemoClass() {
        return mKitDemoClass;
    }

    public Drawable getBgDrawable() {
        return mBgDrawable;
    }

    public String getName() {
        return mKitName;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public String getDocUrl() {
        return mDocUrl;
    }
}
