package me.samlss.inkjet.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.ScreenUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.ui.adapters.ProjectListAdapter;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class ProjectDialog  extends AlertDialog {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private ProjectListAdapter mProjectListAdapter;
    private List<Project> mProjectList;
    private OnItemActionCallBack mItemActionCallBack;

    public ProjectDialog(@NonNull Context context, List<Project> projects) {
        super(context, R.style.PopupDialog);
        mProjectList = projects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_project_list);
        setCanceledOnTouchOutside(false);

        ButterKnife.bind(this);

        mProjectListAdapter = new ProjectListAdapter(R.layout.layout_item_project_list, mProjectList);
        mRecyclerView.setAdapter(mProjectListAdapter);

        mProjectListAdapter.setOnItemClickListener((adapter, view, position) -> {
            mProjectListAdapter.setSelectedIndex(position);
            mProjectListAdapter.notifyDataSetChanged();
        });

        mProjectListAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            if (mItemActionCallBack != null){
                mItemActionCallBack.onItemLongClick(view, position);
            }
            return true;
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (ScreenUtils.getScreenWidth() * 0.9f);
        getWindow().setAttributes(p);
    }

    public void setItemActionCallBack(OnItemActionCallBack itemActionCallBack) {
        this.mItemActionCallBack = itemActionCallBack;
    }

    public interface OnItemActionCallBack{
        void onItemSelect(int position);

        void onItemLongClick(View view, int position);
    }

    private void onConfirm(){
        if (mItemActionCallBack != null){
            mItemActionCallBack.onItemSelect(mProjectListAdapter.getSelectedIndex());
        }
        dismiss();
    }

    private void onCancel(){
        dismiss();
    }

    @OnClick({R.id.btn_confirm, R.id.btn_cancel})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_confirm:
                onConfirm();
                break;

            case R.id.btn_cancel:
                onCancel();
                break;
        }
    }
}
