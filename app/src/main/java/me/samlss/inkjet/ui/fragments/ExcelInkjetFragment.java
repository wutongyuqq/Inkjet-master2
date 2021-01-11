package me.samlss.inkjet.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.samlss.inkjet.R;
import me.samlss.inkjet.ui.base.BaseFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description excel
 */
public class ExcelInkjetFragment extends BaseFragment {
    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_excel_code, null);
        ButterKnife.bind(this, layout);
        initializeBanner(layout.findViewById(R.id.iv_header));
        return layout;
    }

    @OnClick({R.id.layout_data_list, R.id.layout_excel_collection})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.layout_data_list:
                startFragment(new PrintFragment());
                break;

            case R.id.layout_excel_collection:
                startFragment(new FetchExcelFragment());
                break;
        }
    }
}
