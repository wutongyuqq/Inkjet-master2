package me.samlss.inkjet.ui.adapters;

import android.util.SparseArray;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import me.samlss.framework.utils.ResourceUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.bean.SplitDataListBean;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 数据列表适配器
 */
public class SplitDataListAdapter extends BaseQuickAdapter<SplitDataListBean, BaseViewHolder> {

    private List<SparseArray> mSelectedColList;

    public SplitDataListAdapter(int layoutResId, @Nullable List<SplitDataListBean> data) {
        super(layoutResId, data);
        mSelectedColList = new ArrayList<>();
    }

    @Override
    protected void convert(BaseViewHolder helper, SplitDataListBean item) {
        try {
            SparseArray sparseArray = mSelectedColList.get(item.index);

            RecyclerView itemView = helper.getView(R.id.recyclerView);
            DataListAdapter dataListAdapter = new DataListAdapter(R.layout.layout_item_data_column_wrap, item.dataList, sparseArray);
            itemView.setAdapter(dataListAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
            linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
            itemView.setLayoutManager(linearLayoutManager);
            dataListAdapter.setOnItemClickListener((adapter, view, position) -> {
                if (sparseArray.indexOfKey(position) < 0){
                    sparseArray.put(position, 1);
                }else{
                    sparseArray.remove(position);
                }

                adapter.notifyItemChanged(position);
            });

            TextView lineView = helper.getView(R.id.tv_line);
            lineView.setText(ResourceUtils.getString(R.string.split_col, item.index+1));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<SparseArray> getSelectedColList() {
        return mSelectedColList;
    }

    public boolean isSplitColEmpty(){
        boolean empty = true;
        for (int i = 0; i < mSelectedColList.size(); i++){
            if (mSelectedColList.get(i).size() > 0){
                empty = false;
                break;
            }
        }

        return empty;
    }

    public void reset(int count){
       clear();

        for (int i = 0; i < count; i++){
            SparseArray sparseArray = new SparseArray();
            if (getData().size() > 0) {
                for (int j = 0; j < getData().get(0).dataList.size(); j++) {
                    sparseArray.put(j, 1);
                }
            }
            mSelectedColList.add(sparseArray);
        }
    }

    public void clear(){
        for (int i = 0; i < mSelectedColList.size(); i++){
            mSelectedColList.get(i).clear();
        }
        mSelectedColList.clear();
    }
}
