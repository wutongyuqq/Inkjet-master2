package me.samlss.inkjet.ui.adapters;

import android.view.View;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.samlss.inkjet.R;
import me.samlss.inkjet.db.Project;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description just display strings
 */
public class ProjectListAdapter extends BaseQuickAdapter<Project, BaseViewHolder> {
    private int mSelectedIndex;

    public ProjectListAdapter(int layoutResId, @Nullable List<Project> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Project item) {
        helper.setText(R.id.tv_content, item.getProject_name());
        helper.getView(R.id.iv_mark).setVisibility(mSelectedIndex == helper.getAdapterPosition() ? View.VISIBLE : View.INVISIBLE);
    }

    public void setSelectedIndex(int selectedIndex) {
        this.mSelectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }
}
