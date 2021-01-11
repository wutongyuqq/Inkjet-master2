package me.samlss.inkjet.ui.model;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.samlss.framework.utils.FileUtils;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.PathUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.InkAnnal;
import me.samlss.inkjet.ui.fragments.InkAnnalFragment;
import me.samlss.inkjet.utils.InkjetUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码记录逻辑类
 */
public class InkAnnalModel {
    public final static int MAX_PAGE_NUMBER = 15;
    private InkAnnalFragment mInkAnnalFragment;
    private MyTask mGetAnnalsTask;
    private ThreadUtils.Task<List<InkAnnal>> mCheckDataTask;
    private ThreadUtils.Task<String> mGenerateExcelTask;
    private static SimpleDateFormat sFORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public InkAnnalModel(InkAnnalFragment inkAnnalFragment){
        mInkAnnalFragment = inkAnnalFragment;
    }


    private void cancelTask(){
        if (mGetAnnalsTask != null
                && !mGetAnnalsTask.isCanceled()){
            mGetAnnalsTask.cancel();
        }
    }

    public void refreshData(int page) {
        cancelTask();
        mGetAnnalsTask = new MyTask(page, InkConstant.RL_ACTION_REFRESH);
        ThreadUtils.executeByIo(mGetAnnalsTask);
    }

    public void loadMoreData(int page) {
        cancelTask();
        mGetAnnalsTask = new MyTask(page, InkConstant.RL_ACTION_LOADMORE);
        ThreadUtils.executeByIo(mGetAnnalsTask);
    }

    private void cancelCheckDataTask(){
        if (mCheckDataTask != null && !mCheckDataTask.isCanceled()){
            mCheckDataTask.cancel();
        }
    }

    public void checkData(long beginTime, long endTime){
        cancelCheckDataTask();
        mCheckDataTask = new ThreadUtils.Task<List<InkAnnal>>() {
            @Nullable
            @Override
            public List<InkAnnal> doInBackground() throws Throwable {
                return DbManager.get().getAnnals(beginTime, endTime);
            }

            @Override
            public void onSuccess(@Nullable List<InkAnnal> result) {
                mInkAnnalFragment.onCheckDataFinish(result);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                t.printStackTrace();
                mInkAnnalFragment.onCheckDataFinish(null);
            }
        };
        ThreadUtils.executeByIo(mCheckDataTask);
    }

    private String assemblyFileLine(String first, String second){
        if (TextUtils.isEmpty(first)){
            return null;
        }

        if (second == null){
            second = "";
        }
        return first + "\t\t\t" + second + "\n";
    }

    public void generateExcel(final String name, final List<InkAnnal> inkAnnals){
        cancelGenerateTask();
        mGenerateExcelTask = new ThreadUtils.Task<String>() {
            @Nullable
            @Override
            public String doInBackground() throws Throwable {
                if (ListUtils.isEmpty(inkAnnals)){
                    return null;
                }

                try {
                    String path = PathUtils.getExternalStoragePath() + File.separator + "penmayi" + File.separator + name + ".txt";
                    FileUtils.createFileByDeleteOldFile(path);

                    FileOutputStream fos = new FileOutputStream(path);
                    String content = assemblyFileLine("喷码日期", "喷码内容");
                    fos.write(content.getBytes());

                    Date date = new Date();
                    String dateStr;
                    String annalContent;
                    for (InkAnnal annal : inkAnnals) {
                        try {
                            date.setTime(annal.getPrint_time());
                            dateStr = sFORMATTER.format(date);
                            annalContent = InkjetUtils.getInkAnnalContent(annal);

                            content = assemblyFileLine(dateStr, annalContent);
                            if (TextUtils.isEmpty(content)) {
                                continue;
                            }

                            fos.write(content.getBytes());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    return path;
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void onSuccess(@Nullable String result) {
                if (TextUtils.isEmpty(result)){
                    mInkAnnalFragment.onGenerateExcelFailure(null);
                }else{
                    mInkAnnalFragment.onGenerateExcelSuccess(result);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (t != null) {
                    t.printStackTrace();
                }
                mInkAnnalFragment.onGenerateExcelFailure(null);
            }
        };
        ThreadUtils.executeByIo(mGenerateExcelTask);
    }

    private void cancelGenerateTask(){
        if (mGenerateExcelTask != null && !mGenerateExcelTask.isCanceled()){
            mGenerateExcelTask.cancel();
        }
    }

    public void destroy() {
        cancelTask();
        cancelCheckDataTask();
        cancelGenerateTask();
    }

    public class MyTask extends ThreadUtils.Task<List<InkAnnal>>{
        private int mPage;
        private int mAction;

        public MyTask(int pageIndex, int type){
            mPage = pageIndex;
            mAction = type;
        }

        @Nullable
        @Override
        public List<InkAnnal> doInBackground() throws Throwable {
            return DbManager.get().getAnnals(mPage, MAX_PAGE_NUMBER);
        }

        @Override
        public void onSuccess(@Nullable List<InkAnnal> result) {
            if (mAction == InkConstant.RL_ACTION_REFRESH) {
                mInkAnnalFragment.onRefreshSuccess(result);
            } else if (mAction == InkConstant.RL_ACTION_LOADMORE) {
                mInkAnnalFragment.onLoadMoreSuccess(result);
            }
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onFail(Throwable t) {
            if (mAction == InkConstant.RL_ACTION_REFRESH) {
                mInkAnnalFragment.onRefreshFailure(t);
            } else if (mAction == InkConstant.RL_ACTION_LOADMORE) {
                mInkAnnalFragment.onLoadMoreFailure(t);
            }
        }
    }

}
