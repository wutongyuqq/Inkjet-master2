package me.samlss.inkjet.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.framework.utils.ScreenUtils;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.ui.adapters.PrintDataAdapter;
import me.samlss.inkjet.ui.fragments.BasePrintFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description WIFI 创建的项目的显示dialog
 */
public class PrintDialog1 extends AlertDialog {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private PrintDataAdapter mPrintDataAdapter;
    private List<PrintDialog.APrintBean> mPrintList = new ArrayList<>();
    private BasePrintFragment mPrintFragment;

    public PrintDialog1(@NonNull Context context, BasePrintFragment printFragment) {
        super(context, R.style.PopupDialog);
        setCancelable(false);

        mPrintFragment = printFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_print1);

        ButterKnife.bind(this);
        setOnDismissListener(dialog -> mPrintList.clear());

        mPrintDataAdapter = new PrintDataAdapter(R.layout.layout_item_print_data1, mPrintList);
        mRecyclerView.setAdapter(mPrintDataAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (ScreenUtils.getScreenWidth() * 0.9f);
        getWindow().setAttributes(p);
    }

    public void show(PrintBean printBean){
        if (printBean == null || TextUtils.isEmpty(printBean.getSplits())){
            ToastUtils.showShort(R.string.print_state_failed);
            return;
        }

        show();

        try {
            mPrintList.clear();
            List<String> contents = JSON.parseArray(printBean.getSplits(), String.class);
            for (int i = 0; i < contents.size(); i++){
                String content = contents.get(i);
                mPrintList.add(new PrintDialog.APrintBean(content, InkConstant.PRINT_STATE_NONE, i+1));
            }
            mPrintDataAdapter.notifyDataSetChanged();
            mPrintFragment.sendMessageWithStandardFormat(contents.get(contents.size() - 1));
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showShort(R.string.print_state_failed);
        }
    }

    public void onDestroy(){
        mPrintFragment = null;
    }

    public void receivedFinishedMsg(){
        dismiss();
        //打印完毕
        mPrintFragment.printFinish();
    }

    private void showAbortTaskDialog(){
        new Builder(mPrintFragment.getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.abort_task_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    PrintDialog1.this.dismiss();
                    mPrintFragment.cancelPrintTask();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showFinishTaskDialog(){
        new Builder(mPrintFragment.getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.finish_task_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    PrintDialog1.this.dismiss();
                    mPrintFragment.printFinish();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @OnClick({R.id.btn_end, R.id.btn_abort})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_end:
                showFinishTaskDialog();
                break;

            case R.id.btn_abort:
                showAbortTaskDialog();
                break;
        }
    }
}
