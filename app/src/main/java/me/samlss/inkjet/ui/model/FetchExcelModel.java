package me.samlss.inkjet.ui.model;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.ui.fragments.FetchExcelFragment;
import me.samlss.inkjet.utils.ExcelUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description For {@link me.samlss.inkjet.ui.fragments.FetchExcelFragment}
 */
public class FetchExcelModel {
    private FetchExcelFragment mFetchExcelFragment;
    private ThreadUtils.Task<Integer> mParseExcelTask;
    private ThreadUtils.Task<List<String>> mFetchExcelSheetsTask;

    public FetchExcelModel(FetchExcelFragment fetchExcelFragment){
        mFetchExcelFragment = fetchExcelFragment;
    }

    public void start2FetchExcelSheet(File excelFile){
        stop2FetchExcelSheetsTask();

        mFetchExcelSheetsTask = new ThreadUtils.Task<List<String>>() {
            @Nullable
            @Override
            public List<String> doInBackground() throws Throwable {
                return ExcelUtils.getSheets(excelFile);
            }

            @Override
            public void onSuccess(@Nullable List<String> result) {
                if (mFetchExcelFragment != null){
                    mFetchExcelFragment.onFetchSheetsFinished(result);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (mFetchExcelFragment != null) {
                    mFetchExcelFragment.onFetchSheetsFinished(null);
                }
            }
        };

        ThreadUtils.executeByIo(mFetchExcelSheetsTask);
    }


    public void start2ParseExcelTask(int sheetAt, boolean deleteOld, File excelFile){
        stop2ParseExcelTask();
        mParseExcelTask = new ThreadUtils.Task<Integer>() {
            @Nullable
            @Override
            public Integer doInBackground() throws Throwable {
                return ExcelUtils.parseExcel(sheetAt, deleteOld, excelFile);
            }

            @Override
            public void onSuccess(@Nullable Integer result) {
                if (mFetchExcelFragment != null){
                    mFetchExcelFragment.onParseExcelFinished(result == null ? ExcelUtils.FAILED : result);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (mFetchExcelFragment != null) {
                    mFetchExcelFragment.onParseExcelFinished(ExcelUtils.FAILED);
                }
            }
        };

        ThreadUtils.executeByIo(mParseExcelTask);
    }

    private void stop2ParseExcelTask(){
        if (mParseExcelTask != null && !mParseExcelTask.isCanceled()){
            mParseExcelTask.cancel();
        }
    }

    private void stop2FetchExcelSheetsTask(){
        if (mFetchExcelSheetsTask != null && !mFetchExcelSheetsTask.isCanceled()){
            mFetchExcelSheetsTask.cancel();
        }
    }

    public void destroy(){
        stop2ParseExcelTask();
        stop2FetchExcelSheetsTask();
        mFetchExcelFragment = null;
    }
}
