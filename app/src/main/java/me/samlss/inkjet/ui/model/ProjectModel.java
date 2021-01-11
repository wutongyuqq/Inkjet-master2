package me.samlss.inkjet.ui.model;

import androidx.annotation.Nullable;

import java.util.List;

import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.ui.fragments.ProjectFragment;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class ProjectModel {
    public final static int MAX_PAGE_NUMBER = 20;
    private ProjectFragment mProjectFragment;
    private ThreadUtils.Task<List<Project>> mFetchProjectTask;
    private ThreadUtils.Task<Boolean> mDeleteProjectsTask;

    public ProjectModel(ProjectFragment fragment){
        mProjectFragment = fragment;
    }

    public void fetchProject(int pageIndex){
        stopFetchingProjectTask();
        mFetchProjectTask = new ThreadUtils.Task<List<Project>>() {
            @Nullable
            @Override
            public List<Project> doInBackground() throws Throwable {
                return DbManager.get().getProjects(pageIndex, MAX_PAGE_NUMBER);
            }

            @Override
            public void onSuccess(@Nullable List<Project> result) {
                if (mProjectFragment != null){
                    mProjectFragment.onFetchProjectSuccess(result);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (mProjectFragment != null){
                    mProjectFragment.onFetchProjectFailure();
                }
            }
        };

        ThreadUtils.executeByIo(mFetchProjectTask);
    }

    private void stopFetchingProjectTask(){
        if (mFetchProjectTask != null
                && !mFetchProjectTask.isCanceled()){
            mFetchProjectTask.cancel();
        }
    }

    public void deleteProjects(List<Project> projects){
        mDeleteProjectsTask = new ThreadUtils.Task<Boolean>() {
            @Nullable
            @Override
            public Boolean doInBackground() throws Throwable {
                for (Project project : projects){
                    DbManager.get().deleteProjects(project.getProject_name());
                }
                DbManager.get().deleteProjects(projects);
                return true;
            }

            @Override
            public void onSuccess(@Nullable Boolean result) {
                if (mProjectFragment != null){
                    mProjectFragment.onDeleteFinish(true, projects);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (mProjectFragment != null){
                    mProjectFragment.onDeleteFinish(false, projects);
                }
            }
        };

        ThreadUtils.executeByIo(mDeleteProjectsTask);
    }

    private void stopDeletingProjectsTask(){
        if (mDeleteProjectsTask != null
                && !mDeleteProjectsTask.isCanceled()){
            mDeleteProjectsTask.cancel();
        }
    }


    public void destroy(){
        stopFetchingProjectTask();
        stopDeletingProjectsTask();
        mProjectFragment = null;
    }
}
