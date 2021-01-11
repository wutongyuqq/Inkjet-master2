package me.samlss.inkjet.widgets;

import com.chad.library.adapter.base.loadmore.LoadMoreView;

import me.samlss.inkjet.R;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description The default custom {@link LoadMoreView}.
 */
public class DefaultLoadMoreView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return R.layout.layout_defalut_list_load_more;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view;
    }
}
