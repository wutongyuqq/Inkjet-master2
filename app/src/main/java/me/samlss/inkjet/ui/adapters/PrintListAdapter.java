package me.samlss.inkjet.ui.adapters;

import android.view.View;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.samlss.inkjet.R;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.utils.Utils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码适配器
 */
public class PrintListAdapter extends BaseQuickAdapter<PrintBean, BaseViewHolder> {
    private OnPrintButtonClickListener mPrintButtonClickListener;

    public PrintListAdapter(int layoutResId, @Nullable List<PrintBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PrintBean item) {
        helper.setText(R.id.tv_number, String.valueOf(item.getLine_number()));
        helper.setText(R.id.tv_content, item.getContent());
//        helper.setText(R.id.tv_pieces_number, String.valueOf(Utils.getInt(item.getPieces_number())));
        helper.setText(R.id.tv_print_count, String.valueOf(Utils.getInt(item.getPrint_count())));

        helper.getView(R.id.tv_print).setOnClickListener(v -> {
            if (mPrintButtonClickListener != null){
                mPrintButtonClickListener.onClick(v, helper.getAdapterPosition());
            }
        });

//        if (Utils.getInt(item.getPrint_count()) < Utils.getInt(item.getPieces_number())){
//            helper.itemView.setBackgroundColor(Color.parseColor("#FFC125"));
//        }else if (Utils.getInt(item.getPrint_count()) == Utils.getInt(item.getPieces_number())){
//            helper.itemView.setBackgroundColor(Color.parseColor("#23B14D"));
//        }else{
//            helper.itemView.setBackgroundColor(Color.parseColor("#FF4040"));
//        }
    }

    public void setPrintButtonClickListener(OnPrintButtonClickListener mPrintButtonClickListener) {
        this.mPrintButtonClickListener = mPrintButtonClickListener;
    }

    public interface OnPrintButtonClickListener{
        void onClick(View view, int position);
    }

}
