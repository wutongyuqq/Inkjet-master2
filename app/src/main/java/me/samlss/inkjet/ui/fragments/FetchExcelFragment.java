package me.samlss.inkjet.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
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
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.ui.MainActivity;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.dialogs.DialogUtils;
import me.samlss.inkjet.ui.model.DataListModel;
import me.samlss.inkjet.ui.model.DataListPageModel;
import me.samlss.inkjet.ui.model.FetchExcelModel;
import me.samlss.inkjet.ui.permission.RuntimeRationale;
import me.samlss.inkjet.utils.ExcelUtils;
import me.samlss.inkjet.utils.UriUtils;
import me.samlss.ui.arch.QMUIFragment;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description to collect & parse excel data.
 */
public class FetchExcelFragment extends BaseFragment {
    public final static int OPEN_FILE_REQUEST_CODE = 100;
    public final static int PARSE_EXCEL_DATA_LIST_REQUEST_CODE = 101;
    @BindView(R.id.btn_next)
    Button mBtnNext;

    @BindView(R.id.tv_path)
    TextView mTvPath;

    private File mExcelFile;
    private FetchExcelModel mFetchExcelModel;
    private QMUITipDialog mParsingTipDialog;
    private int mExcelSheetIndex;
    private DataListPageModel mDataListPageModel;
    private SparseArray<Integer> mSelectedColArray = new SparseArray<>(); //选中的列表


    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_upload_excel, null);
        ButterKnife.bind(this, layout);

        ViewUtils.setBackground(mBtnNext,
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_open),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        mParsingTipDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在解析文件...")
                .create();

        mParsingTipDialog.setCancelable(false);
        if (InkConfig.getExcelDataPath() != null){
            mExcelFile = new File(InkConfig.getExcelDataPath());
            mTvPath.setText(ResourceUtils.getString(R.string.file_path) + ": " + mExcelFile.getPath());
        }

        initializeBanner(layout.findViewById(R.id.iv_header));
        mFetchExcelModel = new FetchExcelModel(this);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mParsingTipDialog.dismiss();
        mFetchExcelModel.destroy();
    }

    @OnClick({R.id.btn_next, R.id.layout_upload_attachment, R.id.btn_open})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_next:
                checkNext();
                break;

            case R.id.layout_upload_attachment:
                fetchFile();
                break;

            case R.id.btn_open:
                checkOpenFile();
                break;
        }
    }

    private void checkOpenFile(){
        if (mExcelFile == null){
            ToastUtils.showShort(R.string.check_file_not_upload);
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
        try {
            Bundle bundle = new Bundle();
            bundle.putString("path", mExcelFile.getPath());
            DisplayExcelFragment fragment = new DisplayExcelFragment();
            fragment.setArguments(bundle);
            startFragment(fragment);
        }catch (Exception e){
            ToastUtils.showShort(R.string.check_file_failed);
            e.printStackTrace();
        }
    }

    private void fetchFile(){
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
        }catch (Exception e){
            ToastUtils.showShort(R.string.open_file_failed);
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_FILE_REQUEST_CODE) {
                try {
                    Uri uri = data.getData();
                    mExcelFile = UriUtils.uri2File(uri);

                    if (!ExcelUtils.isExcel(mExcelFile.getPath())){
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.wrong_fetch_file_tips)
                                .setNegativeButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                        mExcelFile = null;
                        return;
                    }

                    mTvPath.setText(ResourceUtils.getString(R.string.file_path) + ": " + mExcelFile.getPath());
                }catch (Exception e){
                    ToastUtils.showShort(R.string.open_file_failed);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onFragmentResult(int requestCode, int resultCode, Intent data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == PARSE_EXCEL_DATA_LIST_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                startFragmentAndDestroyCurrent(new PrintFragment());
            }
        }
    }

    private void checkNext(){
        if (mExcelFile == null){
            ToastUtils.showShort(R.string.check_file_not_upload);
            return;
        }

        if (!AppPermissionUtil.hasPermission(AppPermissionConstant.STORAGE_GROUP)){
            PermissionRequester.want(AppPermissionConstant.STORAGE_GROUP)
                    .rationale(new RuntimeRationale(getActivity()))
                    .listen(new RequestListener() {
                        @Override
                        public void onGranted(List<String> grantedPermissions) {
                            start2GetSheets();
                        }

                        @Override
                        public void onDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions) {
                            onPermissionDenied(deniedPermissions, foreverDeniedPermissions);
                        }
                    })
                    .request();
        }else{
            start2GetSheets();
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

    //caused: Can not perform this action after onSaveInstanceState
    //即在任务还没加载完 ， activity 不在当前， 且又启动fragment
    public void startFragment(QMUIFragment fragment) {
        String tagName = fragment.getClass().getSimpleName();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(((MainActivity)getActivity()).getContextViewId(), fragment, tagName)
                .addToBackStack(tagName)
                .commitAllowingStateLoss();
    }

    public void start2GetSheets(){
        if (mExcelFile == null){
            ToastUtils.showShort(R.string.check_file_not_upload);
            return;
        }

        if (!mExcelFile.exists()){
            ToastUtils.showLong(R.string.file_not_exists);
            return;
        }

        mParsingTipDialog.show();
        mFetchExcelModel.start2FetchExcelSheet(mExcelFile);
    }


    public void onFetchSheetsFinished(List<String> sheets){
        mExcelSheetIndex = 0;
        mParsingTipDialog.dismiss();
        if (ListUtils.isEmpty(sheets)){
            ToastUtils.showLong(R.string.parse_excel_failed);
        }else{
            String[] sheetArr = new String[sheets.size()];
            ListUtils.list2Array(sheets, sheetArr);

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.select_sheet)
                    .setSingleChoiceItems(sheetArr, 0, (dialog, which) -> mExcelSheetIndex = which)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        if (mExcelSheetIndex < 0 || mExcelSheetIndex > (sheets.size() - 1)){
                            ToastUtils.showShort(R.string.fetch_data_failed);
                            return;
                        }
                        dialog.dismiss();

                        performParseExcel(false);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void performParseExcel(boolean deleteOld){
        if (mExcelFile == null){
            ToastUtils.showShort(R.string.check_file_not_upload);
            return;
        }

        mParsingTipDialog.show();
        mFetchExcelModel.start2ParseExcelTask(mExcelSheetIndex, deleteOld, mExcelFile);
    }

    public void onParseExcelFinished(int result){
        mParsingTipDialog.dismiss();
        switch (result) {
            case ExcelUtils.SUCCESS:
                InkConfig.setExcelDataPath(mExcelFile.getPath());
                InkConfig.setExcelSheetIndex(mExcelSheetIndex);
                mDataListPageModel = new DataListPageModel(this);
                mDataListPageModel.fetchExcelData();
                //startFragmentForResult(new DataListFragment(), PARSE_EXCEL_DATA_LIST_REQUEST_CODE);
                break;

            case ExcelUtils.FILE_NOT_EXISTS:
                ToastUtils.showLong(R.string.file_not_exists);
                break;

            case ExcelUtils.NO_SHEETS:
                ToastUtils.showLong(R.string.no_sheets);
                break;

            case ExcelUtils.NO_ROWS:
                ToastUtils.showLong(R.string.sheet_no_available_data);
                break;

            case ExcelUtils.EXISTS_PROJECT:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.tip)
                        .setMessage(R.string.exists_project)
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            dialog.dismiss();
                            performParseExcel(true);
                        })
                        .show();
                break;

            case ExcelUtils.FAILED:
            default:
                ToastUtils.showLong(R.string.parse_excel_failed);
                break;
        }
    }

    private DataListPageModel.ProjectPageInfo mProjectInfo;


    public void onFetchExcelFinished(DataListPageModel.ProjectPageInfo projectInfo){


        if (projectInfo == null || ListUtils.isEmpty(projectInfo.columns)){

            return;
        }

        if (ListUtils.isEmpty(projectInfo.columns)){

            return;
        }
        mProjectInfo = projectInfo;
        mDataListPageModel.generateData(0,  12,mProjectInfo);


    }

    public void onGenerateDataSuccess(int printListSize){
        startFragment(new PrintFragment());
    }

    public void onGenerateDataFailure(){
        ToastUtils.showShort(R.string.generate_data_failed);
    }
}
