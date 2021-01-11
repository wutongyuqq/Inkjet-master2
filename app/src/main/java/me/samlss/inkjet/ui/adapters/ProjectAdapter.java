package me.samlss.inkjet.ui.adapters;

import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.db.Project;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description just display strings
 */
public class ProjectAdapter extends BaseQuickAdapter<Project, BaseViewHolder> {
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private boolean isDeleteMode;
    private SparseArray<Boolean> mDeletedArray;

    public ProjectAdapter(int layoutResId, @Nullable List<Project> data, SparseArray<Boolean> deletedArray) {
        super(layoutResId, data);

        mDeletedArray = deletedArray;
    }

    @Override
    protected void convert(BaseViewHolder helper, Project item) {
        ImageView ivState = helper.getView(R.id.iv_state);
        ivState.setVisibility(isDeleteMode ? View.VISIBLE : View.GONE);
        DrawableCompat.setTint(ivState.getDrawable(), mDeletedArray.get(helper.getAdapterPosition(), false) ? ResourceUtils.getColor(R.color.app_color_blue) :
                ResourceUtils.getColor(R.color.qmui_config_color_gray_8));
        try {
            helper.setText(R.id.tv_time, mSimpleDateFormat.format(new Date(item.getGenerate_time())));
            helper.setText(R.id.tv_project_name, item.getProject_name());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return mSimpleDateFormat;
    }

    public void setDeleteMode(boolean deleteMode) {
        isDeleteMode = deleteMode;
        notifyDataSetChanged();
    }
}
