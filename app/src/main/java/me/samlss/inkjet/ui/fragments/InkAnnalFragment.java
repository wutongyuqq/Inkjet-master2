package me.samlss.inkjet.ui.fragments;

import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
import me.samlss.framework.utils.MimeUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.StringUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.framework.utils.UriUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.db.InkAnnal;
import me.samlss.inkjet.ui.adapters.AnnalListAdapter;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.dialogs.DialogUtils;
import me.samlss.inkjet.ui.model.InkAnnalModel;
import me.samlss.inkjet.ui.permission.RuntimeRationale;
import me.samlss.inkjet.utils.ExcelUtils;
import me.samlss.inkjet.utils.MapUtils;
import me.samlss.inkjet.utils.SystemShareUtils;
import me.samlss.inkjet.widgets.DefaultLoadMoreView;
import me.samlss.ui.widget.dialog.QMUIBottomSheet;
import me.samlss.ui.widget.dialog.QMUIDialog;
import me.samlss.ui.widget.dialog.QMUIDialogAction;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码记录
 */
public class InkAnnalFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_no_data)
    TextView mTvNoData;

    private AnnalListAdapter mAnnalListAdapter;
    private List<InkAnnal> mAnnalList = new ArrayList<>();

    private int mPageIndex = 0;
    private InkAnnalModel mInkAnnalModel;

    private String[] mMapNames;
    private QMUITipDialog mHandlingExcelDialog;
    private QMUIDialog.EditTextDialogBuilder mInputNameDialogBuilder;
    private QMUITipDialog mGeneratingExcelDialog;

    @Override
    protected View onCreateView() {
        mInkAnnalModel = new InkAnnalModel(this);
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_ink_record, null);
        ButterKnife.bind(this, layout);

        ViewUtils.setBackground(layout.findViewById(R.id.btn_generate_excel),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_green), DensityUtils.dp2px(5)));

        mMapNames = ResourceUtils.getStringArray(R.array.map_name);
        initRecyclerView();
        performRefresh();
        initializeBanner(layout.findViewById(R.id.iv_header));

        mGeneratingExcelDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.generating_excel))
                .create();

        mGeneratingExcelDialog.setCanceledOnTouchOutside(false);
        mHandlingExcelDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(ResourceUtils.getString(R.string.handling))
                .create();

        mHandlingExcelDialog.setCanceledOnTouchOutside(false);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAnnalList.clear();
        mAnnalListAdapter.notifyDataSetChanged();
        mInkAnnalModel.destroy();
    }

    private void initRecyclerView(){
        mAnnalListAdapter = new AnnalListAdapter(R.layout.layout_item_annal, mAnnalList);
        mAnnalListAdapter.setLoadMoreView(new DefaultLoadMoreView());
        mAnnalListAdapter.setOnLoadMoreListener(mLoadMoreListener, mRecyclerView);

        mAnnalListAdapter.setOnItemClickListener((adapter, view, position) -> new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle(R.string.print_content)
                .setMessage(ExcelUtils.getInkAnnalContent(mAnnalList.get(position)))
                .addAction(R.string.cancel, (dialog, index) -> dialog.dismiss())
                .create(me.samlss.ui.R.style.QMUI_Dialog).show());

        mAnnalListAdapter.setNavigationButtonClickListener((v, position) -> new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem(mMapNames[0])
                .addItem(mMapNames[1])
                .addItem(mMapNames[2])
                .addItem(mMapNames[3])
                .setOnSheetItemClickListener((dialog, itemView, position1, tag) -> {
                    double latitude = mAnnalList.get(position).getLatitude();
                    double longitude = mAnnalList.get(position).getLongitude();
                    String address = StringUtils.nullToEmpty(mAnnalList.get(position).getAddress());
                    try{
                        switch (position1){
                            case 0:
                                if (MapUtils.hasGaodeMap()){
                                    MapUtils.openGaodeMap(address, latitude, longitude);
                                }else {
                                    showNoMapAppDialog(address, latitude, longitude,0);
                                }
                                break;

                            case 1:
                                if (MapUtils.hasTencentMap()){
                                    MapUtils.openTencentMap(address, latitude, longitude);
                                }else {
                                    showNoMapAppDialog(address, latitude, longitude,1);
                                }
                                break;

                            case 2:
                                if (MapUtils.hasBaiduMap()){
                                    MapUtils.openBaiduMap(address, latitude, longitude);
                                }else {
                                    showNoMapAppDialog(address, latitude, longitude,2);
                                }
                                break;

                            case 3:
                                if (MapUtils.hasGoogleMap()){
                                    MapUtils.openGoogleMap(address, latitude, longitude);
                                }else {
                                    showNoMapAppDialog(address, latitude, longitude,3);
                                }
                                break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        ToastUtils.showLong(R.string.open_map_failed);
                    }
                    dialog.dismiss();
                })
                .build()
                .show());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAnnalListAdapter);
    }

    private void showNoMapAppDialog(String address, double latitude, double longitude, int index){
        String mapName = mMapNames[index];
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tip)
                .setMessage(ResourceUtils.getString(R.string.no_map, mapName))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    switch (index){
                        case 0:
                            MapUtils.openGaodeMapByBrowser(address, latitude, longitude);
                            break;

                        case 1:
                            MapUtils.openTencentMapByBrowser(address, latitude, longitude);
                            break;

                        case 2:
                            MapUtils.openBaiduMapByBrowser(address, latitude, longitude);
                            break;

                        case 3:
                            MapUtils.openGoogleMapByBrowser(address, latitude, longitude);
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private BaseQuickAdapter.RequestLoadMoreListener mLoadMoreListener = () -> performLoadmore();

    private void performRefresh(){
        mPageIndex = 0;
        mInkAnnalModel.refreshData(mPageIndex);
    }

    private void performLoadmore(){
        mPageIndex++;
        mInkAnnalModel.loadMoreData(mPageIndex);
    }

    public void onRefreshSuccess(List<InkAnnal> result) {
        if (!ListUtils.isEmpty(result)) {
            mAnnalList.clear();
            mAnnalList.addAll(result);
            mAnnalListAdapter.notifyDataSetChanged();

            if (result.size() < InkAnnalModel.MAX_PAGE_NUMBER) { //判断是否小于一页的数量
                mAnnalListAdapter.loadMoreEnd();
            } else {
                mAnnalListAdapter.setEnableLoadMore(true);
                mAnnalListAdapter.loadMoreComplete();
            }
            mTvNoData.setVisibility(View.GONE);
        } else {
            if (mAnnalList.isEmpty()) {
                mTvNoData.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onLoadMoreSuccess(List<InkAnnal> result) {
        if (ListUtils.isEmpty(result)){
            mAnnalListAdapter.loadMoreEnd();
        }else{
            mAnnalListAdapter.loadMoreComplete();
            if (result.size() < InkAnnalModel.MAX_PAGE_NUMBER){
                mAnnalListAdapter.loadMoreEnd();
            }

            mAnnalList.addAll(result);
            mAnnalListAdapter.notifyDataSetChanged();
        }
    }

    public void onRefreshFailure(Throwable t) {
        mPageIndex--;

        if (mAnnalList.isEmpty()){
            mTvNoData.setVisibility(View.VISIBLE);
        }else {
            mTvNoData.setVisibility(View.GONE);
            ToastUtils.showLong(R.string.load_failed);
        }
    }

    public void onLoadMoreFailure(Throwable t) {
        mPageIndex--;
        mAnnalListAdapter.loadMoreFail();
    }

    private void checkGenerateExcel(){
        if (mAnnalList.isEmpty()){
            ToastUtils.showShort(R.string.no_ink_data);
            return;
        }

        if (!AppPermissionUtil.hasPermission(AppPermissionConstant.STORAGE_GROUP)){
            PermissionRequester.want(AppPermissionConstant.STORAGE_GROUP)
                    .rationale(new RuntimeRationale(getActivity()))
                    .listen(new RequestListener() {
                        @Override
                        public void onGranted(List<String> grantedPermissions) {
                            onGenerateExcel();
                        }

                        @Override
                        public void onDenied(List<String> deniedPermissions, List<String> foreverDeniedPermissions) {
                            onPermissionDenied(deniedPermissions, foreverDeniedPermissions);
                        }
                    })
                    .request();
        }else{
            onGenerateExcel();
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

    public void onGenerateExcel(){
        //时间选择器
        new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                Date beginDate = new Date();
                beginDate.setYear(date.getYear());
                beginDate.setMonth(date.getMonth());
                beginDate.setDate(date.getDate());
                beginDate.setHours(0);
                beginDate.setMinutes(0);
                beginDate.setSeconds(0);

                final long begin = beginDate.getTime();

                new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        Date endDate = new Date();
                        endDate.setYear(date.getYear());
                        endDate.setMonth(date.getMonth());
                        endDate.setDate(date.getDate());
                        endDate.setHours(23);
                        endDate.setMinutes(59);
                        endDate.setSeconds(59);
                        mHandlingExcelDialog.show();
                        mInkAnnalModel.checkData(begin, endDate.getTime());
                    }
                }).setTitleText("请选择结束日期")
                        .build()
                        .show();
            }
        }).setTitleText("请选择起始日期")
                .build()
                .show();
    }

    public void onCheckDataFinish(List<InkAnnal> inkAnnals){
        mHandlingExcelDialog.dismiss();
        if (ListUtils.isEmpty(inkAnnals)){
            ToastUtils.showShort(R.string.no_ink_data);
            return;
        }

        if (mInputNameDialogBuilder == null) {
            mInputNameDialogBuilder = new QMUIDialog.EditTextDialogBuilder(getActivity())
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .addAction(R.string.cancel, (dialog, index) -> dialog.dismiss())
                    .addAction(R.string.confirm, (dialog, index) -> {
                        String input = mInputNameDialogBuilder.getEditText().getText().toString();
                        if (TextUtils.isEmpty(input)){
                            ToastUtils.showShort(R.string.input_name_hint);
                            return;
                        }

                        dialog.dismiss();
                        mGeneratingExcelDialog.show();
                        mInkAnnalModel.generateExcel(input, inkAnnals);
                    });
        }

        mInputNameDialogBuilder.show();
        mInputNameDialogBuilder.getEditText().setHint(R.string.input_name_hint);
    }

    public void onGenerateExcelSuccess(final String path){
        mGeneratingExcelDialog.dismiss();
        QMUIDialog qmuiDialog = new QMUIDialog.MessageDialogBuilder(getActivity())
                .setMessage("生成记录成功，本地文件路径为："+path)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("导出", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        try {
                            File file = new File(path);
                            SystemShareUtils.shareFile(getActivity(), "导出记录", MimeUtils.getMIMEType(file), UriUtils.file2Uri(file));
                        }catch (Exception e){
                            ToastUtils.showShort("导出失败~");
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                })
                .create();

        qmuiDialog.setCancelable(false);
        qmuiDialog.show();
    }

    public void onGenerateExcelFailure(String msg){
        mGeneratingExcelDialog.dismiss();
        if (msg == null){
            msg = ResourceUtils.getString(R.string.generate_excel_failed);
        }

        ToastUtils.showShort(msg);
    }

    @OnClick({R.id.btn_generate_excel})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_generate_excel:
                checkGenerateExcel();
                break;
        }
    }
}
