package me.samlss.inkjet.ui.fragments;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.ui.adapters.ProjectAdapter;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.model.InkAnnalModel;
import me.samlss.inkjet.ui.model.ProjectModel;
import me.samlss.inkjet.widgets.DefaultLoadMoreView;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 项目管理
 */
public class ProjectFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.layout_action)
    View mLayoutAction;

    @BindView(R.id.btn_select_all)
    TextView mTvSelectAll;

    private ProjectModel mProjectModel;
    private QMUITipDialog mLoadingDialog;
    private QMUITipDialog mDeletingDialog;

    private List<Project> mProjectList = new ArrayList<>();
    private ProjectAdapter mProjectAdapter;
    private int mPageIndex = 0;

    @BindView(R.id.tv_no_data)
    TextView mTvNoData;

    private SparseArray<Boolean> mDeletedArray = new SparseArray<>();

    private boolean isDeleteAll;
    private boolean isInDeleteMode;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_project, null);

        ButterKnife.bind(this, layout);
        initializeBanner(layout.findViewById(R.id.iv_header));

        mLoadingDialog = new QMUITipDialog.Builder(getActivity())
                .setTipWord(ResourceUtils.getString(R.string.loading))
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mLoadingDialog.setCancelable(false);

        mDeletingDialog = new QMUITipDialog.Builder(getActivity())
                .setTipWord(ResourceUtils.getString(R.string.deleting))
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mDeletingDialog.setCancelable(false);

        mProjectModel = new ProjectModel(this);

        initRecyclerView();
        mLoadingDialog.show();
        mProjectModel.fetchProject(mPageIndex);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProjectModel.destroy();
        mProjectList.clear();
        mDeletedArray.clear();
        mProjectAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProjectAdapter = new ProjectAdapter(R.layout.layout_item_project, mProjectList, mDeletedArray);

        mRecyclerView.setAdapter(mProjectAdapter);
        mProjectAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (isInDeleteMode){
                if (!mDeletedArray.get(position, false)){
                    mDeletedArray.put(position, true);
                }else{
                    mDeletedArray.remove(position);
                }

                if (mDeletedArray.size() == mProjectList.size() && !isDeleteAll){
                    isDeleteAll = true;
                    mTvSelectAll.setText(R.string.not_select_all);
                }

                else if (mDeletedArray.size() != mProjectList.size() && isDeleteAll){
                    isDeleteAll = false;
                    mTvSelectAll.setText(R.string.select_all);
                }

                adapter.notifyItemChanged(position);
            }else{
            try {
                String message = new StringBuilder().append(ResourceUtils.getString(R.string.create_date))
                            .append("：")
                            .append(mProjectAdapter.getSimpleDateFormat().format(new Date(mProjectList.get(position).getGenerate_time())))
                            .append("\n\n")
                            .append(ResourceUtils.getString(R.string.project_name))
                            .append("：")
                            .append((mProjectList.get(position).getProject_name())).toString();
                new AlertDialog.Builder(getActivity())
                            .setMessage(message)
                            .show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mProjectAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            mProjectAdapter.setDeleteMode(isInDeleteMode = !isInDeleteMode);
            if (isInDeleteMode){
                enterDeleteMode();
            }else{
                exitDeleteMode();
            }
            return true;
        });
        mProjectAdapter.setLoadMoreView(new DefaultLoadMoreView());
        mProjectAdapter.setOnLoadMoreListener(mLoadMoreListener, mRecyclerView);
    }

    private BaseQuickAdapter.RequestLoadMoreListener mLoadMoreListener = () -> performLoadMore();

    private void performLoadMore(){
        mPageIndex++;
        mProjectModel.fetchProject(mPageIndex);
    }

    private void showTipDialog(){
        if (InkConfig.isNoMoreTip4ProjectDeleteDialog()){
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.project_delete_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .setNegativeButton(R.string.no_more_tip, (dialog, which) -> {
                    InkConfig.setNoMoreTip4ProjectDeleteDialog(true);
                    dialog.dismiss();
                })
                .show();
    }

    public void onFetchProjectSuccess(List<Project> projects){
        boolean isLoadMore = mPageIndex > 0;

        mLoadingDialog.dismiss();

        if (isLoadMore) {
            if (ListUtils.isEmpty(projects)){
                mProjectAdapter.loadMoreEnd();
            }else{
                mProjectAdapter.loadMoreComplete();
                if (projects.size() < ProjectModel.MAX_PAGE_NUMBER){
                    mProjectAdapter.loadMoreEnd();
                }

                mProjectList.addAll(projects);
            }

        }else{
            mProjectList.clear();
            if (!ListUtils.isEmpty(projects)) {
                showTipDialog();
                mProjectList.addAll(projects);

                if (projects.size() < InkAnnalModel.MAX_PAGE_NUMBER) { //判断是否小于一页的数量
                    mProjectAdapter.loadMoreEnd();
                } else {
                    mProjectAdapter.setEnableLoadMore(true);
                    mProjectAdapter.loadMoreComplete();
                }
                mTvNoData.setVisibility(View.GONE);
            } else {
                if (mProjectList.isEmpty()) {
                    mTvNoData.setVisibility(View.VISIBLE);
                }
            }
        }
        mProjectAdapter.notifyDataSetChanged();
    }

    public void onFetchProjectFailure(){
        mLoadingDialog.dismiss();
        boolean isLoadMore = mPageIndex > 0;

        if (isLoadMore){
            mPageIndex--;
            mProjectAdapter.loadMoreFail();
        }else{
            if (mProjectList.isEmpty()){
                mTvNoData.setVisibility(View.VISIBLE);
            }else {
                mTvNoData.setVisibility(View.GONE);
                ToastUtils.showLong(R.string.load_failed);
            }
        }
    }

    private void enterDeleteMode(){
        mLayoutAction.setVisibility(View.VISIBLE);
    }

    private void exitDeleteMode(){
        mLayoutAction.setVisibility(View.GONE);
        mProjectAdapter.setDeleteMode(isInDeleteMode = !isInDeleteMode);
    }

    private void onDelete(){
        if (mDeletedArray.size() == 0){
            ToastUtils.showShort(R.string.no_delete_project);
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warn)
                .setMessage(R.string.delete_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    performDelete();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void performDelete(){
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < mDeletedArray.size(); i++){
            projects.add(mProjectList.get(mDeletedArray.keyAt(i)));
        }

        mDeletingDialog.show();
        mProjectModel.deleteProjects(projects);
    }

    public void onDeleteFinish(boolean success, List<Project> projects){
        mDeletingDialog.dismiss();
        if (!success){
            ToastUtils.showShort(R.string.delete_failed_retry);
            return;
        }

        ToastUtils.showShort(R.string.delete_success);
        if (!ListUtils.isEmpty(projects)){
            mDeletedArray.clear();
            mProjectList.removeAll(projects);
            mProjectAdapter.notifyDataSetChanged();
        }

        if (mProjectList.size() == 0){
            mTvNoData.setVisibility(View.VISIBLE);
            exitDeleteMode();
        }
    }

    private void onSelectAll(){
        isDeleteAll = ! isDeleteAll;
        mDeletedArray.clear();

        mTvSelectAll.setText(isDeleteAll ? R.string.not_select_all : R.string.select_all);
        if (isDeleteAll) {
            for (int i = 0; i < mProjectList.size(); i++) {
                mDeletedArray.put(i, true);
            }
        }

        mProjectAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.btn_select_all, R.id.btn_delete, R.id.btn_cancel})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_select_all:
                onSelectAll();
                break;

            case R.id.btn_delete:
                onDelete();
                break;

            case R.id.btn_cancel:
                exitDeleteMode();
                break;
        }
    }
}
