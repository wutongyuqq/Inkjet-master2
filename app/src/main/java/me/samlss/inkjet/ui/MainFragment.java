package me.samlss.inkjet.ui;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.samlss.framework.utils.ToastUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.constant.EventBusDef;
import me.samlss.inkjet.managers.QRCodeManager;
import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.base.BaseRecyclerAdapter;
import me.samlss.inkjet.ui.base.ItemDescription;
import me.samlss.inkjet.ui.base.RecyclerViewHolder;
import me.samlss.inkjet.ui.data.DataManager;
import me.samlss.ui.arch.QMUIFragment;
import me.samlss.ui.widget.dialog.QMUIDialog;
import me.samlss.ui.widget.dialog.QMUITipDialog;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description main page
 */
public class MainFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ItemAdapter mItemAdapter;
    private QRCodeManager mQRCodeManager;
    private QMUITipDialog mGettingDialog;
    private QMUIDialog.EditTextDialogBuilder mInputDiskNumberBuilder;

    @Override
    public void startFragment(QMUIFragment fragment) {
//        super.startFragment(fragment);
        try {
            ((MainActivity)getActivity()).startFragment(fragment);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main, null);
        ButterKnife.bind(this, layout);
        initRecyclerView();

        initializeBanner(layout.findViewById(R.id.iv_header));
        mQRCodeManager = new QRCodeManager(this);
        return layout;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mQRCodeManager != null) {
            mQRCodeManager.destroy();
        }
    }

    @Override
    protected boolean canDragBack() {
        return false;
    }

    private void initRecyclerView() {
        mItemAdapter = new ItemAdapter(getContext(), DataManager.getInstance().getMainItems());
        mItemAdapter.setOnItemClickListener((itemView, pos) -> {
            try {
                if (pos == 3){
                    if (mInputDiskNumberBuilder == null) {
                        mInputDiskNumberBuilder = new QMUIDialog.EditTextDialogBuilder(getActivity())
                                .setInputType(InputType.TYPE_CLASS_TEXT)
                                .addAction(R.string.cancel, (dialog, index) -> dialog.dismiss())
                                .addAction(R.string.confirm, (dialog, index) -> {
                                    String input = mInputDiskNumberBuilder.getEditText().getText().toString();
                                    if (TextUtils.isEmpty(input)){
                                        ToastUtils.showShort(R.string.manual_pan_number_hint);
                                        return;
                                    }

                                    dialog.dismiss();
                                    EventBus.getDefault().post(new EventBusDef.BusBean(EventBusDef.FLAG_QRCODE_RESULT, input));
                                });
                    }

                    mInputDiskNumberBuilder.show();
                    mInputDiskNumberBuilder.getEditText().setHint(R.string.manual_pan_number_hint);
                    return;
                }
                startFragment(DataManager.getInstance().getMainFragments().get(pos).newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        mRecyclerView.setAdapter(mItemAdapter);
        int spanCount = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
    }

    static class ItemAdapter extends BaseRecyclerAdapter<ItemDescription> {

        public ItemAdapter(Context ctx, List<ItemDescription> data) {
            super(ctx, data);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.layout_item_home;
        }

        @Override
        public void bindData(RecyclerViewHolder holder, int position, ItemDescription item) {
            holder.getTextView(R.id.item_name).setText(item.getName());
            holder.getView(R.id.layout_bg).setBackground(item.getBgDrawable());

            if (item.getIconRes() != 0) {
                holder.getImageView(R.id.item_icon).setImageResource(item.getIconRes());
            }
        }
    }
}
