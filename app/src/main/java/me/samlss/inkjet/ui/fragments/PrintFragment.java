package me.samlss.inkjet.ui.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.KeyboardUtils;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.managers.BTConnectManager;
import me.samlss.inkjet.managers.WifiManager;
import me.samlss.inkjet.ui.adapters.PrintListAdapter;
import me.samlss.inkjet.ui.dialogs.PrintDialog;
import me.samlss.inkjet.ui.dialogs.PrintDialog1;
import me.samlss.inkjet.ui.dialogs.ProjectDialog;
import me.samlss.inkjet.ui.model.PrintListModel;
import me.samlss.inkjet.utils.BluetoothUtils;
import me.samlss.inkjet.utils.ExcelUtils;
import me.samlss.inkjet.utils.Utils;
import me.samlss.ui.arch.SwipeBackLayout;
import me.samlss.ui.widget.dialog.QMUIDialog;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 打印页
 */
public class PrintFragment extends BasePrintFragment {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.btn_all)
    TextView mBtnAll;

    @BindView(R.id.et_input)
    EditText mEtInput;

    @BindView(R.id.btn_sprayed)
    TextView mBtnSprayed;

    @BindView(R.id.btn_not_sprayed)
    TextView mBtnNotSprayed;

    @BindView(R.id.tv_tip)
    TextView mTvNoData;

    @BindView(R.id.tv_project)
    TextView mTvProject;

    @BindView(R.id.btn_handle_device)
    Button mBtnHandleDevice;

    private PrintListAdapter mPrintAdapter;
    private List<PrintBean> mPrintList = new ArrayList<>();

    private int mPrintIndex = 0;
    private int mCurrentType = -1;

    private PrintListModel mPrintListModel;
    private List<Project> mProjectList = new ArrayList<>();
    private int mCurrentProjectIndex;
    private QMUITipDialog mLoadingDialog;
    private QMUITipDialog mSearchingDialog;

    private PrintDialog mPrintDialog;
    private PrintDialog1 mPrintDialog1;
    private LinearLayoutManager mLinearLayoutManager;
    private ProjectDialog mProjectDialog;
    //是否跳过选择页面，默认false
    public static boolean isSkipSelectPage = true;

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_print_list, null);
        ButterKnife.bind(this, layout);
        mPrintListModel = new PrintListModel(this);

        initialize();
        initializeBanner(layout.findViewById(R.id.iv_header));
        initRecyclerView();
        initInputView();

        initDialogs();

        layout.findViewById(R.id.tv_search).setOnClickListener(v -> {
            if (getCurrentProject() == null){
                showNoProject();
                return;
            }

            onSearch();
        });

        mLoadingDialog.show();
        mPrintListModel.fetchProjects();
        mBtnNotSprayed.setSelected(true);
        mBtConnectMgr.addListener(mBtConnectListener);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setBackground(mBtnHandleDevice, DrawableUtils.getSelectedSelector(DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.qmui_config_color_gray_5), DensityUtils.dp2px(5))));

        mBtnHandleDevice.setOnClickListener(v -> handleDevice());
        mBtnHandleDevice.setSelected(mBtConnectMgr.isConnected());
        if (mBtConnectMgr.isConnected()){
            setConnectedSuccessUI(mBtConnectMgr.getConnectedDevice());
        }else{
            setConnectedFailureUI();
        }
    }

    private Project getCurrentProject(){
        if (ListUtils.isEmpty(mProjectList)
                || (mCurrentProjectIndex < 0 || mCurrentProjectIndex > (mProjectList.size() - 1))){
            return null;
        }

        try {
            return mProjectList.get(mCurrentProjectIndex);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private String getSearchText(){
        return mEtInput.getText().toString();
    }

    private void initDialogs(){
        mProjectDialog = new ProjectDialog(getActivity(), mProjectList);
        mProjectDialog.setItemActionCallBack(new ProjectDialog.OnItemActionCallBack() {

            @Override
            public void onItemSelect(int position) {
                if (position == mCurrentProjectIndex){
                    return;
                }

                if (ListUtils.isEmpty(mProjectList)){
                    showNoProject();
                    return;
                }

                mCurrentProjectIndex = position;
                onUpdateProject(getCurrentProject().getProject_name());
                mLoadingDialog.show();
                mTvNoData.setVisibility(View.GONE);
                if(isSkipSelectPage){
                    mPrintListModel.fetchPrintSkipPageListData(getCurrentProject().getProject_name(), "", mCurrentType);
                }else {
                    mPrintListModel.fetchPrintListData(getCurrentProject().getProject_name(), "", mCurrentType);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
//                PLog.e("need long click project to: "+project.getProject_name());
            }
        });

        mLoadingDialog = new QMUITipDialog.Builder(getActivity())
                .setTipWord(ResourceUtils.getString(R.string.loading))
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mLoadingDialog.setCancelable(false);

        mSearchingDialog = new QMUITipDialog.Builder(getActivity())
                .setTipWord(ResourceUtils.getString(R.string.searching))
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mSearchingDialog.setCancelable(false);
    }

//    private Handler mHandler = new Handler();
//
//    private Runnable test = new Runnable() {
//        @Override
//        public void run() {
//            PLog.e("test...");
//            if (mPrintDialog.isShowing()) {
//                mPrintDialog.receivedFinishedMsg();
//                mHandler.postDelayed(test, 3 * 1000);
//            }
//        }
//    };

    private void initRecyclerView(){
        ViewUtils.setBackground(mBtnAll,  DrawableUtils.getSelectedSelector(ResourceUtils.getDrawable(R.drawable.sp_annal_condition_btn_selected_bg),
                ResourceUtils.getDrawable(R.drawable.sp_annal_condition_btn_nomal_bg)));
        ViewUtils.setBackground(mBtnSprayed,  DrawableUtils.getSelectedSelector(ResourceUtils.getDrawable(R.drawable.sp_annal_condition_btn_selected_bg),
                ResourceUtils.getDrawable(R.drawable.sp_annal_condition_btn_nomal_bg)));
        ViewUtils.setBackground(mBtnNotSprayed,  DrawableUtils.getSelectedSelector(ResourceUtils.getDrawable(R.drawable.sp_annal_condition_btn_selected_bg),
                ResourceUtils.getDrawable(R.drawable.sp_annal_condition_btn_nomal_bg)));

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mPrintAdapter = new PrintListAdapter(R.layout.layout_item_print, mPrintList);
        mPrintAdapter.setPrintButtonClickListener((view, position) -> {
            mPrintIndex = position;
            executePrint();
        });
        mRecyclerView.setAdapter(mPrintAdapter);

        mPrintAdapter.setOnItemClickListener((adapter, view, position) -> {
            new QMUIDialog.MessageDialogBuilder(getActivity())
                    .setTitle(R.string.print_content)
                    .setMessage(ExcelUtils.getPrintContent(mPrintList.get(position)))
                    .addAction(R.string.cancel, (dialog, index) -> dialog.dismiss())
                    .create(me.samlss.ui.R.style.QMUI_Dialog).show();
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.hideKeyboardWhenTouchViewOutSide(getActivity(), ev, mEtInput);
        return super.dispatchTouchEvent(ev);
    }

    private void initInputView(){
        mEtInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId== EditorInfo.IME_ACTION_DONE||
                    (event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {
                onSearch();
                return true;
            }

            return false;
        });
    }

    private void onSearch(){
        try{
            KeyboardUtils.hideSoftInput(mEtInput);
            mSearchingDialog.show();
            mPrintListModel.fetchPrintSkipPageListData(getCurrentProject().getProject_name(), getSearchText(), mCurrentType);
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showShort(R.string.jump_failed);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPrintList.clear();
        mPrintAdapter.notifyDataSetChanged();

        mPrintListModel.destroy();
        if (mPrintDialog != null && mPrintDialog.isShowing()) {
            mPrintDialog.dismiss();
        }
        if (mPrintDialog1 != null && mPrintDialog1.isShowing()) {
            mPrintDialog1.dismiss();
        }

        if (mProjectDialog.isShowing()){
            mProjectDialog.dismiss();
        }

        mProjectList.clear();
        mBtConnectMgr.removeListener(mBtConnectListener);
    }

    private void showNoProject(){
        mTvNoData.setVisibility(View.VISIBLE);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.no_project_data)
                .setPositiveButton(R.string.upload_attachment, (dialog, which) -> {
                    dialog.dismiss();
                    startFragmentAndDestroyCurrent(new FetchExcelFragment());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void onUpdateProject(String projectName){
        mTvProject.setText(ResourceUtils.getString(R.string.current_project, projectName));
    }

    public void onFetchProjectsFinished(List<Project> projects){
        mLoadingDialog.dismiss();
        if (ListUtils.isEmpty(projects)){
            mTvProject.setText(ResourceUtils.getString(R.string.current_project, ""));
            showNoProject();
            return;
        }

        mProjectList.clear();
        mProjectList.addAll(projects);
        mCurrentProjectIndex = 0;
        onUpdateProject(getCurrentProject().getProject_name());
        onConditionChange(InkConstant.ANNAL_TYPE_NOT_SPRAYED);
    }

    /**
     * 全部/已喷码/未喷码
     * */
    private void onConditionChange(int type){
        if (type == mCurrentType){
            return;
        }

        mBtnAll.setSelected(false);
        mBtnSprayed.setSelected(false);
        mBtnNotSprayed.setSelected(false);

        switch (type){
            case InkConstant.ANNAL_TYPE_ALL:
                mBtnAll.setSelected(true);
                break;

            case InkConstant.INK_STATE_SPRAYED:
                mBtnSprayed.setSelected(true);
                break;

            case InkConstant.INK_STATE_NOT_SPRAYED:
                mBtnNotSprayed.setSelected(true);
                break;
        }

        if (getCurrentProject() == null){
            showNoProject();
            return;
        }

        mCurrentType = type;

        mLoadingDialog.show();
        mTvNoData.setVisibility(View.GONE);
        mPrintListModel.fetchPrintListData(getCurrentProject().getProject_name(), "", type);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH){
            if (resultCode == Activity.RESULT_OK){
                startFragmentForResult(new BtDeviceListFragment(), REQUEST_CODE_START_DEVICE_LIST_PAGE);
            }else{
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.warn)
                        .setMessage(R.string.need_enable_blue_tooth)
                        .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
        }
    }

    private void setConnectedFailureUI(){
        mConnectingDialog.dismiss();
        mBtnHandleDevice.setText(R.string.connect_device);
        mBtnHandleDevice.setSelected(false);
    }

    private void setConnectedSuccessUI(BluetoothDevice device){
        if (device == null){
            return;
        }

        mConnectingDialog.dismiss();
        mBtnHandleDevice.setSelected(true);
        mBtnHandleDevice.setText("已连接"+"(" + BluetoothUtils.getDisplayName(device) + ")");
    }

    private void executePrint() {
        if (getCurrentProject() == null){
            showNoProject();
            return;
        }

        if (this.mBtConnectMgr == null || this.mBtConnectMgr.getState() != BTConnectManager.STATE_CONNECTED) {
            ToastUtils.showShort(R.string.no_connected_bt_device);
            return;
        }

        if (TextUtils.isEmpty(mPrintList.get(mPrintIndex).getContent())){
            ToastUtils.showShort(R.string.no_print_data);
            return;
        }

        try {
            //if (InkConfig.isAutoWifi()) {
                QMUITipDialog loading = new QMUITipDialog.Builder(getActivity())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("蓝牙数据传输中，请稍等...")
                        .create();
                loading.setCancelable(false);
                loading.show();

                List<String> contents = JSON.parseArray(mPrintList.get(mPrintIndex).getSplits(), String.class);
                if (ListUtils.isEmpty(contents)){
                    ToastUtils.showShort(R.string.no_print_data);
                    return;
                }

                final String prjName = "penmayi_"+System.currentTimeMillis();
                ThreadUtils.executeByIo(new ThreadUtils.Task<Boolean>() {
                    @Nullable
                    @Override
                    public Boolean doInBackground() throws Throwable {
                        long before = System.currentTimeMillis();
                        String contentStr = contents.toString();
                        System.out.println("22222---"+contentStr);
                        Character splitChar = Character.valueOf((char) InkConfig.getSplitCharacter());

                        String sFinal = "";
                        for (String content : contents){
                            sFinal += content + splitChar;
                        }
                        int lastTabIndex;
                        if (( lastTabIndex = sFinal.lastIndexOf(splitChar)) > 0){
                            sFinal = sFinal.substring(0, lastTabIndex);
                        }
                        sFinal += Character.valueOf((char) InkConfig.getEndCharacter());
                        sendMessageWithStandardFormat(sFinal);
                        //boolean ret = WifiManager.getInstance().createWifiProject(prjName, true, contents);
                        PLog.e("create proj in time: "+(System.currentTimeMillis() - before));
                        System.out.println("22222---"+sFinal);
                        return true;
                    }

                    @Override
                    public void onSuccess(@Nullable Boolean result) {
                        PLog.e("create: "+result);
                        loading.dismiss();
                        if (result != null && result){
                            WifiManager.getInstance().deleteWifiProjectAsync(InkConfig.getLastWifiPrjName(), true);
                            InkConfig.setLastWifiPrjName(prjName);
                            mPrintDialog1 = new PrintDialog1(getActivity(), PrintFragment.this);
                            mPrintDialog1.show(mPrintList.get(mPrintIndex));
                            printConsumedTime = System.currentTimeMillis();
                            mPrintAdapter.notifyItemChanged(mPrintIndex);
                        }else{
                           showCreateWifiProjFailTip();
                        }
                    }

                    @Override
                    public void onCancel() {
                        loading.dismiss();
                    }

                    @Override
                    public void onFail(Throwable t) {
                        loading.dismiss();
                        showCreateWifiProjFailTip();
                    }
                });
            /*} else {
                mPrintDialog = new PrintDialog(getActivity(), this);
                mPrintDialog.show(mPrintList.get(mPrintIndex));
                printConsumedTime = System.currentTimeMillis();
                mPrintAdapter.notifyItemChanged(mPrintIndex);
            }*/
        }catch (Exception e){
            ToastUtils.showShort("喷码失败，请重新尝试！");
            e.printStackTrace();
        }
    }

    private void showCreateWifiProjFailTip(){
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.tip)
                    .setMessage("创建项目失败，请重新尝试")
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    public void cancelPrintTask(){
        mPrintAdapter.notifyItemChanged(mPrintIndex);
    }

    @Override
    public boolean printFinish(){
        ToastUtils.showShort(R.string.print_state_finish);
        try {
            if (mPrintIndex > (mPrintList.size() - 1)) {
                return false;
            }

            mPrintList.get(mPrintIndex).setPrint_count(Utils.getInt(mPrintList.get(mPrintIndex).getPrint_count()) + 1);
            mPrintList.get(mPrintIndex).setState(InkConstant.PRINT_STATE_FINISH);
            mPrintList.get(mPrintIndex).setTmpState(InkConstant.PRINT_STATE_FINISH);

            DbManager.get().updatePrintBeanAsync(mPrintList.get(mPrintIndex));
            mPrintAdapter.notifyItemChanged(mPrintIndex);

//            String content = ExcelUtils.getPrintContent(mPrintList.get(mPrintIndex));
            mPrintModel.printDone(mPrintList.get(mPrintIndex).getSplits(), System.currentTimeMillis() - printConsumedTime);

//            mHandler.removeCallbacks(test);
            if (InkConfig.getContinuousPrint() && (mPrintIndex < (mPrintList.size() - 1))){
                mPrintIndex++;
                executePrint();
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private BTConnectManager.OnBtConnectListener mBtConnectListener = new BTConnectManager.OnBtConnectListener() {
        @Override
        public void onConnectionFailure(BluetoothDevice device) {
            setConnectedFailureUI();
            ToastUtils.showShort("与"+ BluetoothUtils.getDisplayName(device) + "连接失败~");
        }

        @Override
        public void onConnectionBreak(BluetoothDevice device) {
            setConnectedFailureUI();
            ToastUtils.showShort("与"+ BluetoothUtils.getDisplayName(device) + "连接断开~");
        }

        @Override
        public void onConnectionSuccess(BluetoothDevice device) {
            setConnectedSuccessUI(device);
            ToastUtils.showShort("已连接到设备：" + BluetoothUtils.getDisplayName(device));
        }

        @Override
        public void onReceiveData(BluetoothDevice device, int bytes, byte[] data) {
            try{
                String result = new String(data, 0, bytes, "utf-8");
                PLog.e("result: "+result);

                if (result.equals(InkConfig.getPrintStartReturnValue())){
//                    ToastUtils.showShort(R.string.start_print_des);
                }else if (result.equals(InkConfig.getPrintFinishReturnValue())){
                    ToastUtils.showShort(R.string.finish_print_des);
                    if (mPrintDialog != null && mPrintDialog.isShowing()){
                        mPrintDialog.receivedFinishedMsg();
                    }

                    if (mPrintDialog1.isShowing()){
                        mPrintDialog1.receivedFinishedMsg();
                    }
//                    printFinish();
                } else if(result.equals(InkConfig.getPrintStopReturnValue())){
//                    ToastUtils.showShort(R.string.stop_print_des);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
//            PLog.e("result: "+sb);
        }
    };

    public void onFetchPrintListDataSuccess(List<PrintBean> printBeans){
        mPrintIndex = 0;
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        if (mSearchingDialog.isShowing()) {
            mSearchingDialog.dismiss();
        }
        mPrintList.clear();
        if (!ListUtils.isEmpty(printBeans)) {
            mTvNoData.setVisibility(View.INVISIBLE);
            mRecyclerView.scrollToPosition(0);
            mPrintList.addAll(printBeans);
        }else{
            mTvNoData.setVisibility(View.VISIBLE);
        }
        mPrintAdapter.notifyDataSetChanged();
    }

    public void onFetchPrintListDataFailure(){
        mLoadingDialog.dismiss();
        ToastUtils.showShort(R.string.get_print_list_failed);
        if (ListUtils.isEmpty(mPrintList)){
            mTvNoData.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.btn_all, R.id.btn_sprayed, R.id.btn_not_sprayed, R.id.layout_project})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_print:
                executePrint();
                break;

            case R.id.btn_all:
                onConditionChange(InkConstant.ANNAL_TYPE_ALL);
                break;

            case R.id.btn_sprayed:
                onConditionChange(InkConstant.ANNAL_TYPE_SPRAYED);
                break;

            case R.id.btn_not_sprayed:
                onConditionChange(InkConstant.ANNAL_TYPE_NOT_SPRAYED);
                break;

            case R.id.layout_project:
                if (ListUtils.isEmpty(mProjectList)){
                    showNoProject();
                    return;
                }

                mProjectDialog.show();
                break;
        }
    }
}
