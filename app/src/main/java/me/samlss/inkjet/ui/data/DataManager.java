package me.samlss.inkjet.ui.data;

import java.util.ArrayList;
import java.util.List;

import me.samlss.inkjet.ui.base.BaseFragment;
import me.samlss.inkjet.ui.base.ItemDescription;
import me.samlss.inkjet.ui.fragments.ExcelInkjetFragment;
import me.samlss.inkjet.ui.fragments.FakeManualInputNumberFragment;
import me.samlss.inkjet.ui.fragments.InkAnnalFragment;
import me.samlss.inkjet.ui.fragments.ManualInkFragment;
import me.samlss.inkjet.ui.fragments.PrintSettingsFragment;
import me.samlss.inkjet.ui.fragments.ProjectFragment;
import me.samlss.inkjet.ui.fragments.ScanQRCodeFragment;
import me.samlss.inkjet.ui.fragments.SettingsFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description data manager
 */
public class DataManager {
    private ItemContainer mItemContainer;
    private static DataManager _sInstance;
    private List<Class<? extends BaseFragment>> mMainFragments;

    public DataManager() {
        mItemContainer = ItemContainer.getInstance();
        initMainFragments();
    }

    public static DataManager getInstance() {
        if (_sInstance == null) {
            _sInstance = new DataManager();
        }
        return _sInstance;
    }

    private void initMainFragments() {
        mMainFragments = new ArrayList<>();
        mMainFragments.add(ExcelInkjetFragment.class);
        mMainFragments.add(ManualInkFragment.class);
        //mMainFragments.add(ScanQRCodeFragment.class);
        //mMainFragments.add(FakeManualInputNumberFragment.class);
        mMainFragments.add(ProjectFragment.class);
        mMainFragments.add(InkAnnalFragment.class);
        mMainFragments.add(SettingsFragment.class);
        mMainFragments.add(PrintSettingsFragment.class);
    }

    public List<Class<? extends BaseFragment>> getMainFragments() {
        return mMainFragments;
    }

    public List<ItemDescription> getMainItems(){
        List<ItemDescription> itemDescriptions = new ArrayList<>();
        for (Class clazz : mMainFragments){
            itemDescriptions.add(mItemContainer.get(clazz));
        }

        return itemDescriptions;
    }
}
