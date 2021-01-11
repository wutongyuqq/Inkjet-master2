package me.samlss.inkjet.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.bean.ScanResultBean;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class ScanResultAdapter extends BaseQuickAdapter<ScanResultBean, BaseViewHolder> {
    private SparseArray<Boolean> mSelectedItems;
    private Drawable mSelectedDrawable;
    private Drawable mUnSelectedDrawable;

    public ScanResultAdapter(int layoutResId, @Nullable List<ScanResultBean> data, SparseArray<Boolean> selectedItems) {
        super(layoutResId, data);
        mSelectedItems = selectedItems;
        mSelectedDrawable = DrawableUtils.getRectDrawable(Color.parseColor("#B5E61D"), DensityUtils.dp2px(10));
        mUnSelectedDrawable =  DrawableUtils.getRectDrawable(Color.WHITE, DensityUtils.dp2px(10));

    }

    @Override
    protected void convert(BaseViewHolder helper, ScanResultBean item) {
        helper.setText(R.id.tv_content, item.getKey() + "：" + item.getValue());
        ViewUtils.setBackground(helper.itemView,
                mSelectedItems.get(helper.getAdapterPosition(), false) ? mSelectedDrawable : mUnSelectedDrawable);
    }
}
