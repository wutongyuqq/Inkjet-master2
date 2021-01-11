package me.samlss.inkjet.ui.adapters;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class FontAdapter extends BaseSectionQuickAdapter<FontSectionEntity, BaseViewHolder> {
    public FontAdapter(int layoutResId, int sectionHeadResId, List<FontSectionEntity> data) {
        super(layoutResId, sectionHeadResId, data);
    }

    @Override
    protected void convertHead(BaseViewHolder helper, FontSectionEntity item) {
        ((TextView)(helper.itemView)).setText(item.header);
    }

    @Override
    protected void convert(BaseViewHolder helper, FontSectionEntity item) {
        ((TextView)(helper.itemView)).setText(item.t);
    }
}
