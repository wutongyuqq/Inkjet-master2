package me.samlss.inkjet.ui.adapters;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.ui.dialogs.PrintDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class PrintDataAdapter extends BaseQuickAdapter<PrintDialog.APrintBean, BaseViewHolder> {
    public PrintDataAdapter(int layoutResId, @Nullable List<PrintDialog.APrintBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PrintDialog.APrintBean item) {
        try{
            helper.setText(R.id.tv_content, ResourceUtils.getString(R.string.print_col, item.index) + item.content);
            helper.setText(R.id.tv_state, getStateString(item.state));
            helper.getView(R.id.loading_progress).setVisibility(item.state == InkConstant.PRINT_STATE_PRINTING ? View.VISIBLE : View.INVISIBLE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getStateString(int state){
        switch (state){
            case InkConstant.PRINT_STATE_NONE:
                return ResourceUtils.getString(R.string.not_sprayed_content);

            case InkConstant.PRINT_STATE_FINISH:
                return ResourceUtils.getString(R.string.sprayed_content);

            case InkConstant.PRINT_STATE_PRINTING:
                return ResourceUtils.getString(R.string.print_state_printing);
        }

        return ResourceUtils.getString(R.string.not_sprayed_content);
    }
}
