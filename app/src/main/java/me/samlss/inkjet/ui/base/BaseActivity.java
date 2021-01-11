package me.samlss.inkjet.ui.base;

import me.samlss.inkjet.utils.UmengUtils;
import me.samlss.ui.arch.QMUIActivity;
import me.samlss.ui.util.QMUIDisplayHelper;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class BaseActivity extends QMUIActivity {
    @Override
    protected int backViewInitOffset() {
        return QMUIDisplayHelper.dp2px(getApplicationContext(), 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UmengUtils.doPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UmengUtils.doResume(this);
    }
}
