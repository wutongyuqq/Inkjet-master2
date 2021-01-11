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
import me.samlss.inkjet.ui.fragments.PrintFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class PrintDialog extends AlertDialog {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private PrintDataAdapter  mPrintDataAdapter;
    private List<APrintBean> mPrintList = new ArrayList<>();
    private PrintFragment mPrintFragment;
    private int mPrintIndex = 0;

    public PrintDialog(@NonNull Context context, PrintFragment printFragment) {
        super(context, R.style.PopupDialog);
        setCancelable(false);

        mPrintFragment = printFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_print);

        ButterKnife.bind(this);
        setOnDismissListener(dialog -> {
            mPrintList.clear();
            mPrintDataAdapter.notifyDataSetChanged();
        });

        mPrintDataAdapter = new PrintDataAdapter(R.layout.layout_item_print_data, mPrintList);
        mPrintDataAdapter.setOnItemClickListener((adapter, view, position) -> new Builder(mPrintFragment.getActivity())
                .setMessage(mPrintList.get(position).content)
                .show());
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
                if (TextUtils.isEmpty(content)){
                    continue;
                }

                mPrintList.add(new APrintBean(content, InkConstant.PRINT_STATE_NONE, i+1));
            }
            mPrintDataAdapter.notifyDataSetChanged();
            mPrintIndex = 0;
            onPrint();
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showShort(R.string.print_state_failed);
        }
    }

    public void onPrint(){
        if (mPrintIndex >= mPrintList.size()){
            receivedFinishedMsg();
            return;
        }

        mPrintList.get(mPrintIndex).state = InkConstant.PRINT_STATE_PRINTING;
        mPrintDataAdapter.notifyItemChanged(mPrintIndex);
        mRecyclerView.scrollToPosition(mPrintIndex);
        mPrintFragment.sendMessageWithStandardFormat(mPrintList.get(mPrintIndex).content);
    }

    public void receivedFinishedMsg(){
        if (mPrintIndex >= (mPrintList.size() - 1)){
            //打印完毕
            if (!mPrintFragment.printFinish()){
                dismiss();
            }
            return;
        }

        try {
            mPrintList.get(mPrintIndex).state = InkConstant.PRINT_STATE_FINISH;
            mPrintDataAdapter.notifyItemChanged(mPrintIndex);

            mPrintIndex++;
            onPrint();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showAbortTaskDialog(){
        new Builder(mPrintFragment.getActivity())
                .setTitle(R.string.tip)
                .setMessage(R.string.abort_task_tip)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    PrintDialog.this.dismiss();
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
                    PrintDialog.this.dismiss();
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

    public static class APrintBean{
        public String content;
        public int state;
        public int index;

        public APrintBean(String content, int state, int index) {
            this.content = content;
            this.state = state;
            this.index = index;
        }
    }
}
