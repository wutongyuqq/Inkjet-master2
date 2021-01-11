package me.samlss.inkjet.ui.model;

import androidx.annotation.Nullable;

import java.util.List;

import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.ui.fragments.PrintFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class PrintListModel {
    private PrintFragment mPrintFragment;
    private ThreadUtils.Task<List<PrintBean>> mFetchPrintListTask;
    private ThreadUtils.Task<List<Project>> mFetchProjectsTask;

    public PrintListModel(PrintFragment fragment){
        mPrintFragment = fragment;
    }

    public void fetchPrintListData(String project, String search, int type){
        stopFetchingPrintListDataTask();
        mFetchPrintListTask = new ThreadUtils.Task<List<PrintBean>>() {
            @Nullable
            @Override
            public List<PrintBean> doInBackground() throws Throwable {
                return DbManager.get().getPrintList(project, search, type);
            }

            @Override
            public void onSuccess(@Nullable List<PrintBean> result) {
                mPrintFragment.onFetchPrintListDataSuccess(result);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                mPrintFragment.onFetchPrintListDataFailure();
            }
        };
        ThreadUtils.executeByIo(mFetchPrintListTask);
    }



    //跳过选择页面进行选择
    public void fetchPrintSkipPageListData(String project, String search, int type){
        stopFetchingPrintListDataTask();
        mFetchPrintListTask = new ThreadUtils.Task<List<PrintBean>>() {
            @Nullable
            @Override
            public List<PrintBean> doInBackground() throws Throwable {
                return DbManager.get().getPrintSkipPageList(project, search, type);
            }

            @Override
            public void onSuccess(@Nullable List<PrintBean> result) {
                mPrintFragment.onFetchPrintListDataSuccess(result);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                mPrintFragment.onFetchPrintListDataFailure();
            }
        };
        ThreadUtils.executeByIo(mFetchPrintListTask);
    }


    public void fetchProjects(){
        mFetchProjectsTask = new ThreadUtils.Task<List<Project>>() {
            @Nullable
            @Override
            public List<Project> doInBackground() throws Throwable {
                return DbManager.get().getProjects();
            }

            @Override
            public void onSuccess(@Nullable List<Project> result) {
                mPrintFragment.onFetchProjectsFinished(result);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                mPrintFragment.onFetchProjectsFinished(null);
            }
        };

        ThreadUtils.executeByIo(mFetchProjectsTask);
    }

    private void stopFetchingPrintListDataTask(){
        if (mFetchPrintListTask != null && !mFetchPrintListTask.isCanceled()){
            mFetchPrintListTask.cancel();
        }
    }

    private void stopFetchingProjectsTask(){
        if (mFetchProjectsTask != null && !mFetchProjectsTask.isCanceled()){
            mFetchProjectsTask.cancel();
        }
    }

    public void destroy(){
        stopFetchingPrintListDataTask();
        stopFetchingProjectsTask();
    }
}
