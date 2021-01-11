package me.samlss.inkjet.ui.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.samlss.inkjet.R;
import me.samlss.inkjet.db.InkAnnal;
import me.samlss.inkjet.utils.ExcelUtils;
import me.samlss.inkjet.utils.InkjetUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码记录适配器
 */
public class AnnalListAdapter extends BaseQuickAdapter<InkAnnal, BaseViewHolder> {
    private OnNavigationButtonClickListener mNavigationButtonClickListener;

    public AnnalListAdapter(int layoutResId, @Nullable List<InkAnnal> data) {
        super(layoutResId, data);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void convert(BaseViewHolder helper, InkAnnal item) {
        helper.setText(R.id.tv_content, "喷码内容："+ ExcelUtils.getInkAnnalContent(item));
        helper.setText(R.id.tv_location, "坐标："+ "(" + item.getLatitude() + ", " + item.getLongitude() + ")");

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(item.getPrint_time()));

            helper.setText(R.id.tv_year, String.valueOf(calendar.get(Calendar.YEAR)));
            helper.setText(R.id.tv_day, getTime(calendar.get(Calendar.MONTH) + 1) + "/" + getTime(calendar.get(Calendar.DAY_OF_MONTH)));
            helper.setText(R.id.tv_time, getTime(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTime(calendar.get(Calendar.MINUTE)) + ":"+ getTime(calendar.get(Calendar.SECOND)));
        }catch (Exception e){
            e.printStackTrace();
        }

        helper.getView(R.id.tv_navigation).setOnClickListener(v -> {
            if (mNavigationButtonClickListener != null){
                mNavigationButtonClickListener.onClick(v, helper.getAdapterPosition());
            }
        });
    }

    private String getTime(int time){
        if (time < 10){
            return "0" + time;
        }

        return String.valueOf(time);
    }

    public void setNavigationButtonClickListener(OnNavigationButtonClickListener mNavigationButtonClickListener) {
        this.mNavigationButtonClickListener = mNavigationButtonClickListener;
    }

    public interface OnNavigationButtonClickListener{
        void onClick(View v, int position);
    }
}
