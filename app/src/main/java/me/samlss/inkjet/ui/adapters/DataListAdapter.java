package me.samlss.inkjet.ui.adapters;

import android.graphics.Color;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 数据列表适配器
 */
public class DataListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    private SparseArray<Integer> mSelectedArray; //选中的列表
    public DataListAdapter(int layoutResId, @Nullable List<String> data,
                           SparseArray<Integer> selectedColArray) {
        super(layoutResId, data);
        mSelectedArray = selectedColArray;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        if (mSelectedArray.indexOfKey(helper.getAdapterPosition()) >= 0){
            ViewUtils.setBackground(helper.itemView, DrawableUtils.getRectDrawable(Color.parseColor("#B5E61D"), DensityUtils.dp2px(10)));
        }else{
            ViewUtils.setBackground(helper.itemView, DrawableUtils.getRectDrawable(Color.WHITE, DensityUtils.dp2px(10)));
        }
        helper.setText(R.id.tv_name, item);
    }
}
