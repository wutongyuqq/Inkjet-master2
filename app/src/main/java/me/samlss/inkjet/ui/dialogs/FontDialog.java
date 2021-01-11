package me.samlss.inkjet.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.samlss.framework.utils.ScreenUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.ui.adapters.FontAdapter;
import me.samlss.inkjet.ui.adapters.FontSectionEntity;
import me.samlss.inkjet.ui.fragments.PrintFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class FontDialog extends AlertDialog {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private FontAdapter mFontAdapter;
    private List<FontSectionEntity> mFonts;

    private OnItemSelectedListener mOnItemSelectedListener;

    public FontDialog(@NonNull Context context) {
        super(context, R.style.PopupDialog);
    }

    private void initFonts(){
        mFonts = new ArrayList<>();
        mFonts.add(new FontSectionEntity(true, "EBS内部字体", "", 0));
        mFonts.add(new FontSectionEntity(false, "", "Font_12x7", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Font_16x10", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Font_25x19", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Font_32x24", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Font_5x5", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Font_7x5", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Spec_16", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "Spec_25", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "china_11", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(false, "", "china_16", FontSectionEntity.TYPE_INTERNAL));
        mFonts.add(new FontSectionEntity(true, "用户选择字体", "", 0));
        mFonts.add(new FontSectionEntity(false, "", "MTCORSVA", FontSectionEntity.TYPE_USER));
        mFonts.add(new FontSectionEntity(false, "", "dl1", FontSectionEntity.TYPE_USER));
        mFonts.add(new FontSectionEntity(true, "系统默认字体", "", 0));
        mFonts.add(new FontSectionEntity(false, "", "AARDC___" ,FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "ACTIONIS",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "AGENTORANGE",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "ANGLESMF",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Achafexp",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Achaflft",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Achafont",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Achafsex",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Actionwd",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Actionwi",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "Ayuma2yk",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "BILLD___",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSans-Bold",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSans-BoldOblique",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSansMono",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerif-Bold",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerif-BoldItalic",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerif-Italic",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerif",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerifCondensed-Bold",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerifCondensed-BoldItalic",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerifCondensed-Italic",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "DejaVuLGCSerifCondensed",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeMono",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeMonoBold",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeMonoBoldOblique",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeMonoOblique",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSans",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSansBold",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSansBoldOblique",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSansOblique",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSerif",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSerifBold",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "FreeSerifItalic",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "SIMSUN",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "boston",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "gunplay3",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
        mFonts.add(new FontSectionEntity(false, "", "sqr721bc",FontSectionEntity.TYPE_SYSTEM_DEFAULT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_font);

        ButterKnife.bind(this);
        initFonts();

        mFontAdapter = new FontAdapter(R.layout.layout_item_font_text, R.layout.layout_item_font_header, mFonts);
        mFontAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (mOnItemSelectedListener != null){
                    FontSectionEntity entity = mFonts.get(position);
                    if (entity.isHeader){
                        return;
                    }

                    //Default/name.xml
                    //user/name.ttf
                    //Default/name.ttf

                    String fontParam = null;
                    switch (entity.type){
                        case FontSectionEntity.TYPE_INTERNAL:
                            fontParam = "Default/" + entity.t + ".xml";
                            break;

                        case FontSectionEntity.TYPE_USER:
                            fontParam = "User/" + entity.t + ".ttf";
                            break;

                        case FontSectionEntity.TYPE_SYSTEM_DEFAULT:
                            if (entity.t.equalsIgnoreCase("SIMSUN")){
                                fontParam = "Default/" + entity.t + ".TTF";
                            }else {
                                fontParam = "Default/" + entity.t + ".ttf";
                            }
                            break;
                    }
                    mOnItemSelectedListener.onItemSelect(position, entity.type, entity.t, fontParam);
                }
            }
        });
        mRecyclerView.setAdapter(mFontAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (ScreenUtils.getScreenWidth() * 0.9f);
        getWindow().setAttributes(p);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        this.mOnItemSelectedListener = itemSelectedListener;
    }

    public interface OnItemSelectedListener{
        void onItemSelect(int position, int type, String displayFont, String paramFont);
    }
}
