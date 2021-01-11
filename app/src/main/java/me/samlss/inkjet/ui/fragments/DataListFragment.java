package me.samlss.inkjet.ui.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.permission.AppPermissionConstant;
import me.samlss.framework.permission.AppPermissionUtil;
import me.samlss.framework.permission.PermissionRequester;
import me.samlss.framework.permission.RequestListener;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.bean.SplitDataListBean;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.ui.adapters.DataListAdapter;
import me.samlss.inkjet.ui.adapters.SplitDataListAdapter;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.dialogs.DialogUtils;
import me.samlss.inkjet.ui.model.DataListModel;
import me.samlss.inkjet.ui.permission.RuntimeRationale;
import me.samlss.inkjet.utils.Utils;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 数据列表
 */
public class DataListFragment extends BaseFragment {

    @BindView(R.id.scrollView)
    ScrollView mScrollView;

    @BindView(R.id.recyclerView)
    RecyclerView mDataListView;

    @BindView(R.id.tv_row_range)
    TextView mTvRowRange;

    @BindView(R.id.edit_begin_row)
    EditText mEtBeginRow;

    @BindView(R.id.edit_end_row)
    EditText mEtEndRow;

    @BindView(R.id.btn_generate_data)
    Button mBtnGenerateData;

    @BindView(R.id.layout_list)
    ViewGroup mLayoutDataList;

    @BindView(R.id.layout_input_row)
    ViewGroup mLayoutInputRow;

    @BindView(R.id.layout_select_split)
    ViewGroup mLayoutSelectSplit;

    @BindView(R.id.rv_split)
    RecyclerView mSplitRecyclerView;

    @BindView(R.id.sp_data_col)
    AppCompatSpinner mSpDataCol;

    private SplitDataListAdapter mSplitDataListAdapter;
    private List<SplitDataListBean> mSplitColNameList = new ArrayList<>(); //分隔list

    private DataListAdapter mDataListAdapter;

    private DataListModel mDataListModel;
    private QMUITipDialog mLoadingDialog;
    private QMUITipDialog mChangingSheetDialog;
    private QMUITipDialog mGeneratingDialog;

    private DataListModel.ProjectInfo mProjectInfo;
    private List<String> mColNameList = new ArrayList<>(); //列名

    private SparseArray<Integer> mSelectedColArray = new SparseArray<>(); //选中的列表

    private final static int STEP_SHOW_LIST = 0;
    private final static int STEP_SELECTED_SPLIT = 1;
    private final static int STEP_INPUT_ROW = 2;

    private int mStep = STEP_SHOW_LIST;

    @Override
    protected View onCreateView() {
        setFragmentResult(RESULT_CANCELED, null);
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_data_list, null);
        ButterKnife.bind(this, layout);

        initDataListView();
        initLoadingDialog();
        initSplitViews();

        ViewUtils.setBackground(layout.findViewById(R.id.btn_open_file),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_generate_data),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        if (TextUtils.isEmpty(InkConfig.getExcelDataPath())){
            showDialogOfExcelFileNotExists();
        }else {
            mLoadingDialog.show();
            mDataListModel = new DataListModel(this);
            mDataListModel.fetchExcelData();
        }

        initializeBanner(layout.findViewById(R.id.iv_header));
        return layout;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtBeginRow);
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtEndRow);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDataListModel != null) {
            mDataListModel.destroy();
        }

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (mChangingSheetDialog.isShowing()){
            mChangingSheetDialog.dismiss();
        }

        if (mGeneratingDialog.isShowing()){
            mGeneratingDialog.dismiss();
        }

        mColNameList.clear();
        mSelectedColArray.clear();
        mSplitColNameList.clear();
        mSplitDataListAdapter.clear();
    }

    private void initDataListView(){
        mDataListAdapter = new DataListAdapter(R.layout.layout_item_data_column, mColNameList, mSelectedColArray);
        mDataListView.setAdapter(mDataListAdapter);
        mDataListView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mDataListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //不是第一个的格子都设一个左边和底部的间距
                outRect.left = DensityUtils.dp2px(10);
            }
        });
        mDataListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (mColNameList.get(position).equals(ResourceUtils.getString(R.string.pieces_number))){
                ToastUtils.showShort(R.string.must_select_pieces_column);
                return;
            }

            if (mSelectedColArray.indexOfKey(position) < 0){
                mSelectedColArray.put(position, 1);
            }else{
                mSelectedColArray.remove(position);
            }

            mDataListAdapter.notifyItemChanged(position);
        });
    }

    private void initLoadingDialog(){
        mLoadingDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.loading))
                .create();
        mLoadingDialog.setCancelable(false);

        mChangingSheetDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.switching))
                .create();
        mChangingSheetDialog.setCancelable(false);

        mGeneratingDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.generating))
                .create();
        mGeneratingDialog.setCancelable(false);
    }

    private void initSplitViews(){
        mSplitDataListAdapter = new SplitDataListAdapter(R.layout.layout_item_data_column_split, mSplitColNameList);
        mSplitRecyclerView.setAdapter(mSplitDataListAdapter);
        mSplitRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSpDataCol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSplitCols(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onFetchExcelFinished(DataListModel.ProjectInfo projectInfo){
        mLoadingDialog.dismiss();

        if (projectInfo == null || ListUtils.isEmpty(projectInfo.columns)){
            showDialogOfFetchDataFailed();
            return;
        }

        if (ListUtils.isEmpty(projectInfo.columns)){
            showDialogOfFetchDataNoColumns();
            return;
        }

        mProjectInfo = projectInfo;
        updateRowNumber();
        updateExcelColumns();
    }

    private void updateExcelColumns(){
        mColNameList.clear();
        mSplitColNameList.clear();

        if (!ListUtils.isEmpty(mProjectInfo.columns)) {
            mColNameList.addAll(mProjectInfo.columns);
        }

//        if (mProjectInfo.columns != null){
//            String contentRow = ResourceUtils.getString(R.string.content);
//            int index;
//            if ((index = mProjectInfo.columns.indexOf(contentRow)) >= 0) {
//                mSelectedColArray.put(index, 1);
//            }
//        }
        for (int i = 0; i < mColNameList.size(); i++){
            mSelectedColArray.put(i, 1);
        }

        mDataListAdapter.notifyDataSetChanged();
    }

    private void updateSplitCols(int count){
        List<String> selectedCols = new ArrayList<>();
        for (int colIndex = 0; colIndex < mSelectedColArray.size(); colIndex++){
            selectedCols.add(mProjectInfo.columns.get(mSelectedColArray.keyAt(colIndex)));
        }

        mSplitColNameList.clear();
        for (int i = 0; i < count; i++){
            mSplitColNameList.add(new SplitDataListBean(i, selectedCols));
        }

        mSplitDataListAdapter.reset(mSplitColNameList.size());
        mSplitDataListAdapter.notifyDataSetChanged();
    }

    private void updateRowNumber(){
        mTvRowRange.setText(String.format("(1 - %d)", Utils.getInt(mProjectInfo.rowNumber)));
    }

    private void showDialogOfExcelFileNotExists(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.check_file_no_data)
                .setPositiveButton(R.string.upload_attachment, (dialog, which) -> {
                    dialog.dismiss();
                    startFragmentAndDestroyCurrent(new FetchExcelFragment());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void showDialogOfFetchDataFailed(){
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.fetch_data_failed)
                .setPositiveButton(R.string.upload_attachment, (dialog, which) -> {
                    dialog.dismiss();
                    startFragmentAndDestroyCurrent(new FetchExcelFragment());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    private void showDialogOfFetchDataNoColumns(){
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.no_variable_data)
                .setPositiveButton(R.string.upload_attachment, (dialog, which) -> {
                    dialog.dismiss();
                    startFragmentAndDestroyCurrent(new FetchExcelFragment());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    private void checkOpenFile(){
        if (TextUtils.isEmpty(InkConfig.getExcelDataPath()) || mProjectInfo == null){
            showDialogOfExcelFileNotExists();
            return;
        }

        if (!AppPermissionUtil.hasPermission(AppPermissionConstant.STORAGE_GROUP)){
            PermissionRequester.want(AppPermissionConstant.STORAGE_GROUP)
                    .rationale(new RuntimeRationale(getActivity()))
                    .listen(new RequestListener() {
                        @Override
                        public void onGranted(List<String> grantedPermissions) {
                            openFile();
                        }

                        @Override
                        public void onDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions) {
                            onPermissionDenied(deniedPermissions, foreverDeniedPermissions);
                        }
                    })
                    .request();
        }else{
            openFile();
        }
    }

    private void openFile() {
        if (TextUtils.isEmpty(InkConfig.getExcelDataPath()) || mProjectInfo == null){
            showDialogOfExcelFileNotExists();
            return;
        }

        try {
            Bundle bundle = new Bundle();
            bundle.putString("path", InkConfig.getExcelDataPath());
            DisplayExcelFragment fragment = new DisplayExcelFragment();
            fragment.setArguments(bundle);
            startFragment(fragment);
        }catch (Exception e){
            ToastUtils.showShort(R.string.check_file_failed);
            e.printStackTrace();
        }
    }

    private void onPermissionDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions){
        if (!foreverDeniedPermissions.isEmpty()){
            DialogUtils.showRejectedPermissionGotToAppSettingDialog(getActivity());
            return;
        }

        if (!deniedPermissions.isEmpty()){
            DialogUtils.showRejectedPermissionTip(getActivity());
        }
    }

    private void onGenerateData(){
        if (mProjectInfo == null || ListUtils.isEmpty(mProjectInfo.columns)){
            showDialogOfFetchDataFailed();
            return;
        }


        if (TextUtils.isEmpty(mEtBeginRow.getText().toString())){
            ToastUtils.showShort(R.string.plz_input_begin_row);
            return;
        }

        if (TextUtils.isEmpty(mEtEndRow.getText().toString())){
            ToastUtils.showShort(R.string.plz_input_end_row);
            return;
        }

        if (mProjectInfo.rowNumber < 1){
            ToastUtils.showShort(R.string.sheet_no_available_data);
            return;
        }

        try {
            int beginRowNum = Integer.valueOf(mEtBeginRow.getText().toString());
            int endRowNum = Integer.valueOf(mEtEndRow.getText().toString());

            if (beginRowNum < 1){
                ToastUtils.showShort(R.string.plz_input_less_1_row);
                return;
            }

            if (endRowNum > mProjectInfo.rowNumber){
                ToastUtils.showShort(R.string.plz_input_less_most_row, mProjectInfo.rowNumber);
                return;
            }

            if (endRowNum < beginRowNum){
                ToastUtils.showShort(R.string.end_can_not_less_than_begin);
                return;
            }
            mGeneratingDialog.show();
            mDataListModel.generateData(beginRowNum, endRowNum, mProjectInfo, mSelectedColArray, mSplitDataListAdapter.getSelectedColList());
        }catch (Exception e){
            ToastUtils.showShort(R.string.generate_data_failed);
            e.printStackTrace();
        }
    }

    public void onGenerateDataSuccess(int printListSize){
        mGeneratingDialog.dismiss();
        setFragmentResult(RESULT_OK, null);
        popBackStack();
//        startFragmentAndDestroyCurrent(new PrintFragment());
    }

    public void onGenerateDataFailure(){
        mGeneratingDialog.dismiss();
        ToastUtils.showShort(R.string.generate_data_failed);
    }

    private void onSelectedColumns(){
        mLayoutDataList.setVisibility(View.GONE);
        mLayoutSelectSplit.setVisibility(View.VISIBLE);
        mSplitDataListAdapter.notifyDataSetChanged();
        mScrollView.scrollTo(0, 0);
        updateSplitCols(1);
    }

    private void onSelectedSplits(){
        mLayoutSelectSplit.setVisibility(View.GONE);
        mLayoutInputRow.setVisibility(View.VISIBLE);

        mScrollView.scrollTo(0, 0);
        mStep = STEP_INPUT_ROW;
    }

    private void next(){
        if (TextUtils.isEmpty(InkConfig.getExcelDataPath()) || mProjectInfo == null){
            showDialogOfExcelFileNotExists();
            return;
        }

        if (ListUtils.isEmpty(mProjectInfo.columns)){
            ToastUtils.showLong(R.string.empty_sheet);
            return;
        }

        if (mSelectedColArray.size() == 0){
            ToastUtils.showLong(R.string.plz_select_col);
            return;
        }

        //标准版无需包含
//        if (mProjectInfo.columns != null
//                && !mProjectInfo.columns.contains(ResourceUtils.getString(R.string.pieces_number))){
//            ToastUtils.showShort(R.string.excel_must_include_pieces_column);
//            return;
//        }

        switch (mStep){
            case STEP_SHOW_LIST:
                onSelectedColumns();
                mStep = STEP_SELECTED_SPLIT;
                break;

            case STEP_SELECTED_SPLIT:
                if (mSplitDataListAdapter.isSplitColEmpty()){
                    ToastUtils.showShort(R.string.select_split_warn);
                    return;
                }

                mEtBeginRow.setText(String.valueOf(1));
                mEtEndRow.setText(String.valueOf(Utils.getInt(mProjectInfo.rowNumber)));
                onSelectedSplits();
                mBtnGenerateData.setText(R.string.generate_data);
                break;

            case STEP_INPUT_ROW:
                onGenerateData();
                break;
        }
    }

    @OnClick({R.id.btn_open_file, R.id.btn_generate_data, R.id.layout_split})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_open_file:
                checkOpenFile();
                break;

            case R.id.btn_generate_data:
                next();
                break;

            case R.id.layout_split:
                mSpDataCol.performClick();
                break;
        }
    }
}
