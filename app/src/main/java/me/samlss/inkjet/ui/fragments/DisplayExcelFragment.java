package me.samlss.inkjet.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.DensityUtils;
import me.samlss.framework.utils.DrawableUtils;
import me.samlss.framework.utils.PathUtils;
import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.UriUtils;
import me.samlss.framework.utils.ViewUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.ui.base.BaseFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 显示excel文件
 */
public class DisplayExcelFragment extends BaseFragment implements TbsReaderView.ReaderCallback {

    @BindView(R.id.layout_error)
    View mErrorLayout;

    private String mExcelPath;
    private TbsReaderView mTbsReaderView;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        mExcelPath = args.getString("path");
    }

    @Override
    protected View onCreateView() {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_display_excel, null);
        ButterKnife.bind(this, layout);

        ViewUtils.setBackground(layout.findViewById(R.id.btn_open_with_others),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_blue), DensityUtils.dp2px(5)));

        ViewUtils.setBackground(layout.findViewById(R.id.btn_retry),
                DrawableUtils.getRectDrawable(ResourceUtils.getColor(R.color.app_color_blue), DensityUtils.dp2px(5)));

        mTbsReaderView = new TbsReaderView(getActivity(), this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        mTbsReaderView.setLayoutParams(layoutParams);
        ((ViewGroup)layout.findViewById(R.id.fl_rootview)).addView(mTbsReaderView);

        displayExcelImmediately();
        initializeBanner(layout.findViewById(R.id.iv_header));
        return layout;
    }

    private void displayExcelImmediately(){
        Bundle bundle = new Bundle();
        bundle.putString("filePath", mExcelPath);
        bundle.putString("tempPath", PathUtils.getExternalAppCachePath());

        // preOpen 需要文件后缀名 用以判断是否支持
        boolean result = mTbsReaderView.preOpen(parseFormat(mExcelPath), true);
        if (result) {
            mTbsReaderView.openFile(bundle);
            mTbsReaderView.setVisibility(View.VISIBLE);
            mErrorLayout.setVisibility(View.GONE);
        } else {
            mTbsReaderView.setVisibility(View.GONE);
            mErrorLayout.setVisibility(View.VISIBLE);
        }
    }

    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    @Override
    public void onCallBackAction(Integer integer, Object long1, Object long2) {
//        PLog.e("onCallBackAction " + integer + "," + long1 + "," + long2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
    }

    private void openFileWithOtherApps(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置标记
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(UriUtils.file2Uri(new File(mExcelPath)), "*/*");//设置类型
        startActivity(intent);
    }

    @OnClick({R.id.btn_retry, R.id.btn_open_with_others})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_retry:
                displayExcelImmediately();
                break;

            case R.id.btn_open_with_others:
                openFileWithOtherApps();
                break;
        }
    }
}
